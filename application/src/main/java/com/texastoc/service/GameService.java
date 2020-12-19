package com.texastoc.service;

import com.texastoc.connector.EmailConnector;
import com.texastoc.connector.SMSConnector;
import com.texastoc.connector.WebSocketConnector;
import com.texastoc.controller.request.CreateGamePlayerRequest;
import com.texastoc.controller.request.UpdateGamePlayerRequest;
import com.texastoc.exception.GameInProgressException;
import com.texastoc.exception.GameIsFinalizedException;
import com.texastoc.model.config.TocConfig;
import com.texastoc.model.game.*;
import com.texastoc.model.season.QuarterlySeason;
import com.texastoc.model.season.Season;
import com.texastoc.model.user.Player;
import com.texastoc.model.user.Role;
import com.texastoc.repository.*;
import com.texastoc.service.calculator.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class GameService {

  private final RoleRepository roleRepository;
  private final GameRepository gameRepository;
  private final SeatingRepository seatingRepository;
  private final PlayerRepository playerRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final GamePayoutRepository gamePayoutRepository;
  private final QuarterlySeasonRepository qSeasonRepository;
  private final SeasonService seasonService;

  private final GameCalculator gameCalculator;
  private final PayoutCalculator payoutCalculator;
  private final PointsCalculator pointsCalculator;
  private final ConfigRepository configRepository;
  private final SeasonCalculator seasonCalculator;
  private final QuarterlySeasonCalculator qSeasonCalculator;

  private final SMSConnector smsConnector;
  private final EmailConnector emailConnector;
  private final WebSocketConnector webSocketConnector;

  private TocConfig tocConfig;
  private ExecutorService executorService;

  public GameService(GameRepository gameRepository, PlayerRepository playerRepository, GamePlayerRepository gamePlayerRepository, GamePayoutRepository gamePayoutRepository, QuarterlySeasonRepository qSeasonRepository, SeasonService seasonService, GameCalculator gameCalculator, PayoutCalculator payoutCalculator, PointsCalculator pointsCalculator, ConfigRepository configRepository, SeasonCalculator seasonCalculator, QuarterlySeasonCalculator qSeasonCalculator, SeatingRepository seatingRepository, RoleRepository roleRepository, SMSConnector smsConnector, EmailConnector emailConnector, WebSocketConnector webSocketConnector) {
    this.gameRepository = gameRepository;
    this.playerRepository = playerRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.gamePayoutRepository = gamePayoutRepository;
    this.qSeasonRepository = qSeasonRepository;
    this.seasonService = seasonService;
    this.gameCalculator = gameCalculator;
    this.payoutCalculator = payoutCalculator;
    this.pointsCalculator = pointsCalculator;
    this.configRepository = configRepository;
    this.seasonCalculator = seasonCalculator;
    this.qSeasonCalculator = qSeasonCalculator;
    this.seatingRepository = seatingRepository;
    this.roleRepository = roleRepository;
    this.smsConnector = smsConnector;
    this.emailConnector = emailConnector;
    this.webSocketConnector = webSocketConnector;

    executorService = Executors.newCachedThreadPool();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public Game createGame(Game game) {
    Season currentSeason = seasonService.getCurrentSeason();

    // Make sure no other game is open
    List<Game> otherGames = gameRepository.getBySeasonId(currentSeason.getId());
    for (Game otherGame : otherGames) {
      if (!otherGame.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    Game gameToCreate = new Game();

    // TODO check that date is allowed - not before an existing game and not beyond the season.
    gameToCreate.setDate(game.getDate());

    QuarterlySeason currentQSeason = qSeasonRepository.getByDate(game.getDate());
    gameToCreate.setSeasonId(currentQSeason.getSeasonId());
    gameToCreate.setQSeasonId(currentQSeason.getId());
    gameToCreate.setQuarter(currentQSeason.getQuarter());
    gameToCreate.setQuarterlyGameNum(currentQSeason.getNumGamesPlayed() + 1);

    Player player = playerRepository.get(game.getHostId());
    gameToCreate.setHostId(game.getHostId());
    gameToCreate.setHostName(player.getName());

    // Game setup variables
    gameToCreate.setTransportRequired(game.isTransportRequired());

    gameToCreate.setKittyCost(currentSeason.getKittyPerGame());
    gameToCreate.setBuyInCost(currentSeason.getBuyInCost());
    gameToCreate.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    gameToCreate.setRebuyAddOnTocDebit(currentSeason.getRebuyAddOnTocDebit());
    gameToCreate.setAnnualTocCost(currentSeason.getTocPerGame());
    gameToCreate.setQuarterlyTocCost(currentSeason.getQuarterlyTocPerGame());
    gameToCreate.setSeasonGameNum(currentSeason.getNumGamesPlayed() + 1);

    gameToCreate.setBuyInCost(currentSeason.getBuyInCost());
    gameToCreate.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    gameToCreate.setRebuyAddOnTocDebit(currentSeason.getRebuyAddOnTocDebit());

    int id = gameRepository.save(gameToCreate);
    gameToCreate.setId(id);

    Game gameCreated = populateGame(gameToCreate);
    sendUpdatedGame();
    return gameCreated;
  }

  @Transactional(readOnly = true)
  public Game getGame(int id) {
    Game game = gameRepository.getById(id);
    return populateGame(game);
  }

  @CacheEvict(value = "currentGame", allEntries = true)
  public void geClearCacheGame() {
  }

  @Transactional(readOnly = true)
  @Cacheable("currentGame")
  public Game getCurrentGame() {
    int seasonId = seasonService.getCurrentSeason().getId();
    List<Game> games = gameRepository.getUnfinalized(seasonId);
    if (games.size() > 0) {
      Game game = games.get(0);
      populateGame(game);
      return game;
    }

    games = gameRepository.getMostRecent(seasonId);
    if (games.size() > 0) {
      Game game = games.get(0);
      populateGame(game);
      return game;
    }

    return null;
  }

  @Transactional(readOnly = true)
  public List<Game> getGames(Integer seasonId) {
    if (seasonId == null) {
      seasonId = seasonService.getCurrentSeason().getId();
    }

    return gameRepository.getBySeasonId(seasonId);
  }

  public void notifySeating(int gameId) {
    Seating seating = seatingRepository.get(gameId);
    if (seating == null || seating.getTables() == null || seating.getTables().size() == 0) {
      return;
    }
    for (Table table : seating.getTables()) {
      if (table.getSeats() == null || table.getSeats().size() == 0) {
        continue;
      }
      for (Seat seat : table.getSeats()) {
        if (seat == null) {
          continue;
        }
        GamePlayer gamePlayer = gamePlayerRepository.selectById(seat.getGamePlayerId());
        Player player = playerRepository.get(gamePlayer.getPlayerId());
        if (player.getPhone() != null) {
          smsConnector.text(player.getPhone(), player.getName() + " table " +
            table.getNumber() + " seat " + seat.getSeatNumber());
        }
      }
    }
  }

  private Game populateGame(Game game) {
    List<GamePlayer> players = gamePlayerRepository.selectByGameId(game.getId());
    game.setPlayers(players);
    int numPaidPlayers = 0;
    int numPaidPlayersRemaining = 0;
    for (GamePlayer player : players) {
      if (player.getBuyInCollected() != null && player.getBuyInCollected() > 0) {
        ++numPaidPlayers;
        if (player.getKnockedOut() == null || !player.getKnockedOut()) {
          ++numPaidPlayersRemaining;
        }
      }
    }
    game.setNumPaidPlayers(numPaidPlayers);
    game.setNumPaidPlayersRemaining(numPaidPlayersRemaining);

    game.setPayouts(gamePayoutRepository.getByGameId(game.getId()));

    Seating seating = new Seating();
    try {
      seating = seatingRepository.get(game.getId());
    } catch (Exception e) {
      // do nothing
    }
    game.setSeating(seating);
    return game;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void updateGame(Game game) {
    Game currentGame = gameRepository.getById(game.getId());
    checkFinalized(currentGame);
    gameRepository.update(game);
    sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createGamePlayer(int gameId, CreateGamePlayerRequest cgpr) {
    Game game = gameRepository.getById(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = GamePlayer.builder()
      .playerId(cgpr.getPlayerId())
      .gameId(gameId)
      .buyInCollected(cgpr.isBuyInCollected() ? game.getBuyInCost() : null)
      .annualTocCollected(cgpr.isAnnualTocCollected() ? game.getAnnualTocCost() : null)
      .quarterlyTocCollected(cgpr.isQuarterlyTocCollected() ? game.getQuarterlyTocCost() : null)
      .build();

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer updateGamePlayer(int gameId, int gamePlayerId, UpdateGamePlayerRequest ugpr) {
    Game game = gameRepository.getById(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = getGamePlayer(gamePlayerId);
    if (gameId != gamePlayer.getGameId()) {
      log.error("Cannot update game player, game id does not match");
      throw new RuntimeException("Cannot update game player, game id does not match");
    }

    Integer place = ugpr.getPlace();
    gamePlayer.setPlace(place);
    gamePlayer.setRoundUpdates(ugpr.isRoundUpdates());
    gamePlayer.setBuyInCollected(ugpr.isBuyInCollected() ? game.getBuyInCost() : null);
    gamePlayer.setRebuyAddOnCollected(ugpr.isRebuyAddOnCollected() ? game.getRebuyAddOnCost() : null);
    gamePlayer.setAnnualTocCollected(ugpr.isAnnualTocCollected() ? game.getAnnualTocCost() : null);
    gamePlayer.setQuarterlyTocCollected(ugpr.isQuarterlyTocCollected() ? game.getQuarterlyTocCost() : null);
    gamePlayer.setChop(ugpr.getChop());

    if (place != null && place <= 10) {
      gamePlayer.setKnockedOut(true);
    } else {
      gamePlayer.setKnockedOut(ugpr.isKnockedOut());
    }

    gamePlayerRepository.update(gamePlayer);

    recalculate(game);
    sendUpdatedGame();
    return gamePlayer;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer toogleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    Game game = gameRepository.getById(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = gamePlayerRepository.selectById(gamePlayerId);
    Boolean knockedOut = gamePlayer.getKnockedOut();
    if (knockedOut == null) {
      knockedOut = true;
    } else {
      knockedOut = !knockedOut;
    }
    gamePlayer.setKnockedOut(knockedOut);

    gamePlayerRepository.update(gamePlayer);

    recalculate(game);
    sendUpdatedGame();
    return gamePlayer;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer toogleGamePlayerRebuy(int gameId, int gamePlayerId) {
    Game game = gameRepository.getById(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = gamePlayerRepository.selectById(gamePlayerId);
    Integer rebuy = gamePlayer.getRebuyAddOnCollected();
    if (rebuy == null || rebuy != game.getRebuyAddOnCost()) {
      rebuy = game.getRebuyAddOnCost();
    } else {
      rebuy = null;
    }
    gamePlayer.setRebuyAddOnCollected(rebuy);

    gamePlayerRepository.update(gamePlayer);

    recalculate(game);
    sendUpdatedGame();
    return gamePlayer;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    GamePlayer gamePlayer = gamePlayerRepository.selectById(gamePlayerId);
    checkFinalized(gamePlayer.getGameId());

    gamePlayerRepository.deleteById(gameId, gamePlayerId);

    Game currentGame = gameRepository.getById(gamePlayer.getGameId());
    recalculate(currentGame);
    sendUpdatedGame();
  }

  @Transactional(readOnly = true)
  public GamePlayer getGamePlayer(int gamePlayerId) {
    return gamePlayerRepository.selectById(gamePlayerId);
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createFirstTimeGamePlayer(int gameId, FirstTimeGamePlayer firstTimeGamePlayer) {
    Game game = gameRepository.getById(gameId);
    checkFinalized(game);

    String firstName = firstTimeGamePlayer.getFirstName();
    String lastName = firstTimeGamePlayer.getLastName();
    Player player = Player.builder()
      .firstName(firstName)
      .lastName(lastName)
      .email(firstTimeGamePlayer.getEmail())
      .build();
    int playerId = playerRepository.save(player);
    roleRepository.save(playerId);

    StringBuilder name = new StringBuilder();
    name.append(!Objects.isNull(firstName) ? firstName : "");
    name.append((!Objects.isNull(firstName) && !Objects.isNull(lastName)) ? " " : "");
    name.append(!Objects.isNull(lastName) ? lastName : "");

    GamePlayer gamePlayer = GamePlayer.builder()
      .gameId(gameId)
      .playerId(playerId)
      .name(name.toString())
      .buyInCollected(firstTimeGamePlayer.isBuyInCollected() ? game.getBuyInCost() : null)
      .annualTocCollected(firstTimeGamePlayer.isAnnualTocCollected() ? game.getAnnualTocCost() : null)
      .quarterlyTocCollected(firstTimeGamePlayer.isQuarterlyTocCollected() ? game.getQuarterlyTocCost() : null)
      .build();

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = {"currentGame", "currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void endGame(int id) {
    Game game = gameRepository.getById(id);
    recalculate(game);
    game = gameRepository.getById(id);
    game.setFinalized(true);
    // TODO set game.chopped
    gameRepository.update(game);
    qSeasonCalculator.calculate(game.getQSeasonId());
    seasonCalculator.calculate(game.getSeasonId());
    seatingRepository.deleteByGameId(id);
    sendUpdatedGame();
  }

  // TODO pulled this out of the endGame method because there seems to be
  // a transactional problem
  public void sendSummary(int gameId) {
    sendGameSummary(gameId);
  }


  @CacheEvict(value = {"currentGame", "currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  public void openGame(int id) {
    Game gameToOpen = gameRepository.getById(id);

    Season season = seasonService.getSeason(gameToOpen.getSeasonId());
    if (season.isFinalized()) {
      // TODO throw exception and handle in RestControllerAdvise
      throw new RuntimeException("Cannot open a game when season is finalized");
    }

    // Make sure no other game is open
    List<Game> games = gameRepository.getBySeasonId(gameToOpen.getSeasonId());
    for (Game game : games) {
      if (game.getId() == gameToOpen.getId()) {
        continue;
      }
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    gameToOpen.setFinalized(false);
    gameRepository.update(gameToOpen);
    sendUpdatedGame();
  }

  // Worker to avoid one @Transacation calling anther @Transactional
  private GamePlayer createGamePlayerWorker(GamePlayer gamePlayer, Game game) {

    if (gamePlayer.getName() == null) {
      Player player = playerRepository.get(gamePlayer.getPlayerId());
      gamePlayer.setName(player.getName());
    }
    gamePlayer.setQSeasonId(game.getQSeasonId());
    gamePlayer.setSeasonId(game.getSeasonId());

    int gamePlayerId = gamePlayerRepository.save(gamePlayer);
    gamePlayer.setId(gamePlayerId);

    recalculate(game);

    return gamePlayer;
  }

  // TODO separate thread
  private void recalculate(Game game) {
    List<GamePlayer> gamePlayers = gamePlayerRepository.selectByGameId(game.getId());
    Game calculatedGame = gameCalculator.calculate(game, gamePlayers);
    payoutCalculator.calculate(calculatedGame, gamePlayers);
    pointsCalculator.calculate(calculatedGame, gamePlayers);
  }

  // TODO fix this now that there is no double buyin
  private void verifyGamePlayerMoney(boolean doubleBuyIn, GamePlayer gamePlayer) {
//    TocConfig tocConfig = getTocConfig();
//    Integer buyIn = gamePlayer.getBuyInCollected();
//    Integer rebuyAddOn = gamePlayer.getRebuyAddOnCollected();
//    Integer toc = gamePlayer.getAnnualTocCollected();
//    Integer qToc = gamePlayer.getQuarterlyTocCollected();
//
//    if (doubleBuyIn) {
//      if (buyIn != null && buyIn != tocConfig.getDoubleBuyInCost()) {
//        throw new DoubleBuyInMismatchException("Buy-in should be double");
//      }
//      if (rebuyAddOn != null && rebuyAddOn != tocConfig.getDoubleRebuyCost()) {
//        throw new DoubleBuyInMismatchException("Rebuy/AddOn should be double");
//      }
//    } else {
//      if (buyIn != null && buyIn == tocConfig.getDoubleBuyInCost()) {
//        throw new DoubleBuyInMismatchException("Buy-in should no be double");
//      }
//      if (rebuyAddOn != null && rebuyAddOn == tocConfig.getDoubleRebuyCost()) {
//        throw new DoubleBuyInMismatchException("Rebuy/AddOn should not be double");
//      }
//    }
//
//    if (toc != null && toc != tocConfig.getAnnualTocCost()) {
//      throw new DoubleBuyInMismatchException("Annual TOC incorrect");
//    }
//    if (qToc != null && qToc != tocConfig.getQuarterlyTocCost()) {
//      throw new DoubleBuyInMismatchException("Quarterly TOC incorrect");
//    }

  }

  // Cache it
  private TocConfig getTocConfig() {
    if (tocConfig == null) {
      tocConfig = configRepository.get();
    }
    return tocConfig;
  }

  private void checkFinalized(int id) {
    checkFinalized(gameRepository.getById(id));
  }

  private void checkFinalized(Game game) {
    if (game.isFinalized()) {
      throw new GameIsFinalizedException("Game is finalized");
    }
  }

  private void sendGameSummary(int id) {
    SendGameSummary sgs = new SendGameSummary(id);
    new Thread(sgs).start();
  }

  private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();

  static {
    VELOCITY_ENGINE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    VELOCITY_ENGINE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    VELOCITY_ENGINE.init();
  }

  private String getGameSummaryFromTemplate(Game game) {
    Template t = VELOCITY_ENGINE.getTemplate("game-summary.vm");
    VelocityContext context = new VelocityContext();

    context.put("game", game);

    boolean chopped = false;
    for (GamePlayer gamePlayer : game.getPlayers()) {
      if (gamePlayer.getChop() != null && gamePlayer.getChop() > 0) {
        chopped = true;
        break;
      }
    }
    context.put("gameChopped", chopped);

    Season season = seasonService.getSeason(game.getSeasonId());
    context.put("season", season);

    QuarterlySeason currentQSeason = qSeasonRepository.getByDate(game.getDate());
    for (QuarterlySeason qs : season.getQuarterlySeasons()) {
      if (qs.getId() == currentQSeason.getId()) {
        currentQSeason = qs;
      }
    }
    context.put("qSeason", currentQSeason);

    StringWriter writer = new StringWriter();
    t.merge(context, writer);
    return writer.toString();
  }

  private class SendGameSummary implements Runnable {

    private int gameId;

    public SendGameSummary(int gameId) {
      this.gameId = gameId;
    }

    @Override
    public void run() {
      Game game = getGame(gameId);

      String body = getGameSummaryFromTemplate(game);
      String subject = "Summary " + game.getDate();

      List<Player> players = playerRepository.get();
      for (Player player : players) {
        Player playerWithRoles = playerRepository.get(player.getId());
        boolean isAdmin = false;
        for (Role role : playerWithRoles.getRoles()) {
          if ("ADMIN".equals(role.getName())) {
            isAdmin = true;
            break;
          }
        }

        if (isAdmin && !StringUtils.isBlank(player.getEmail())) {
          emailConnector.send(player.getEmail(), subject, body);
        }
      }
    }
  }

  private void sendUpdatedGame() {
    executorService.submit(new GameSender());
  }

  private class GameSender implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      webSocketConnector.sendGame(getCurrentGame());
      return null;
    }
  }
}
