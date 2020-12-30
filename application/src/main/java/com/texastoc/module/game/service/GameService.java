package com.texastoc.module.game.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.notification.connector.SMSConnector;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.SeasonModuleFactory;
import com.texastoc.module.season.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.settings.SettingsModule;
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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final GameCalculator gameCalculator;
  private final PayoutCalculator payoutCalculator;
  private final PointsCalculator pointsCalculator;

  // TODO move to the notification module
  private final SMSConnector smsConnector;
  private final EmailConnector emailConnector;
  private final WebSocketConnector webSocketConnector;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private ExecutorService executorService;
  private SettingsModule settingsModule;

  public GameService(GameRepository gameRepository, GameCalculator gameCalculator, PayoutCalculator payoutCalculator, PointsCalculator pointsCalculator, SMSConnector smsConnector, EmailConnector emailConnector, WebSocketConnector webSocketConnector) {
    this.gameRepository = gameRepository;
    this.gameCalculator = gameCalculator;
    this.payoutCalculator = payoutCalculator;
    this.pointsCalculator = pointsCalculator;
    this.smsConnector = smsConnector;
    this.emailConnector = emailConnector;
    this.webSocketConnector = webSocketConnector;

    executorService = Executors.newCachedThreadPool();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public Game create(Game game) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Season currentSeason = getSeasonModule().getCurrentSeason();

    // TODO check that date is allowed - not before an existing game and not beyond the season.

    // Make sure no other game is open
    List<Game> otherGames = gameRepository.findBySeasonId(currentSeason.getId());
    for (Game otherGame : otherGames) {
      if (!otherGame.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    QuarterlySeason currentQSeason = getSeasonModule().getQuarterlySeasonByDate(game.getDate());
    game.setSeasonId(currentQSeason.getSeasonId());
    game.setQSeasonId(currentQSeason.getId());
    game.setQuarter(currentQSeason.getQuarter());
    game.setQuarterlyGameNum(currentQSeason.getNumGamesPlayed() + 1);

    Player player = getPlayerModule().get(game.getHostId());
    game.setHostName(player.getName());

    // Game setup variables
    game.setKittyCost(currentSeason.getKittyPerGame());
    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebit(currentSeason.getRebuyAddOnTocDebit());
    game.setAnnualTocCost(currentSeason.getTocPerGame());
    game.setQuarterlyTocCost(currentSeason.getQuarterlyTocPerGame());
    game.setSeasonGameNum(currentSeason.getNumGamesPlayed() + 1);

    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebit(currentSeason.getRebuyAddOnTocDebit());

    game = gameRepository.save(game);

    // TODO do we need populateGame anymore?
    //Game gameCreated = populateGame(gameCreated);
    sendUpdatedGame();
    return game;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void update(Game game) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game currentGame = get(game.getId());
    checkFinalized(currentGame);
    currentGame.setHostId(game.getHostId());
    currentGame.setDate(game.getDate());
    currentGame.setTransportRequired(game.isTransportRequired());
    gameRepository.save(currentGame);
    sendUpdatedGame();
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    Optional<Game> optionalGame = gameRepository.findById(id);
    if (!optionalGame.isPresent()) {
      throw new NotFoundException("Game with id " + id + " not found");
    }
    return optionalGame.get();
  }

  @CacheEvict(value = "currentGame", allEntries = true)
  public void geClearCacheGame() {
  }

  @Transactional(readOnly = true)
  @Cacheable("currentGame")
  public Game getCurrent() {
    int seasonId = getSeasonModule().getCurrentSeasonId();
    List<Game> games = gameRepository.findUnfinalizedBySeasonId(seasonId);
    if (games.size() > 0) {
      Game game = games.get(0);
      return game;
    }

    games = gameRepository.findMostRecentBySeasonId(seasonId);
    if (games.size() > 0) {
      Game game = games.get(0);
      return game;
    }

    throw new NotFoundException("Current game not found");
  }

  @Transactional(readOnly = true)
  public List<Game> getBySeasonId(Integer seasonId) {
    if (seasonId == null) {
      seasonId = getSeasonModule().getCurrentSeasonId();
    }

    return gameRepository.findBySeasonId(seasonId);
  }

  @Transactional(readOnly = true)
  public List<Game> getByQuarterlySeasonId(Integer qSeasonId) {
    return gameRepository.findBySeasonId(qSeasonId);
  }

  // TODO move to notifications
  public void notifySeating(int gameId) {
//    Seating seating = seatingRepository.get(gameId);
//    if (seating == null || seating.getTables() == null || seating.getTables().size() == 0) {
//      return;
//    }
//    for (Table table : seating.getTables()) {
//      if (table.getSeats() == null || table.getSeats().size() == 0) {
//        continue;
//      }
//      for (Seat seat : table.getSeats()) {
//        if (seat == null) {
//          continue;
//        }
//        GamePlayer gamePlayer = gamePlayerRepository.selectById(seat.getGamePlayerId());
//        Player player = getPlayerModule().get(gamePlayer.getPlayerId());
//        if (player.getPhone() != null) {
//          smsConnector.text(player.getPhone(), player.getName() + " table " +
//            table.getNumber() + " seat " + seat.getSeatNumber());
//        }
//      }
//    }
  }

  // TODO is this needed anymore? I think not
//  private Game populateGame(Game game) {
//    List<GamePlayer> players = gamePlayerRepository.selectByGameId(game.getId());
//    game.setPlayers(players);
//    int numPaidPlayers = 0;
//    int numPaidPlayersRemaining = 0;
//    for (GamePlayer player : players) {
//      if (player.getBuyInCollected() != null && player.getBuyInCollected() > 0) {
//        ++numPaidPlayers;
//        if (player.getKnockedOut() == null || !player.getKnockedOut()) {
//          ++numPaidPlayersRemaining;
//        }
//      }
//    }
//    game.setNumPaidPlayers(numPaidPlayers);
//    game.setNumPaidPlayersRemaining(numPaidPlayersRemaining);
//
//    game.setPayouts(gamePayoutRepository.getByGameId(game.getId()));
//
//    Seating seating = new Seating();
//    try {
//      seating = seatingRepository.get(game.getId());
//    } catch (Exception e) {
//      // do nothing
//    }
//    game.setSeating(seating);
//    return game;
//  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
    // TODO validate
    Game game = get(gamePlayer.getGameId());
    checkFinalized(game);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    // TODO validate
    Game game = get(gamePlayer.getGameId());
    checkFinalized(game);

    String firstName = gamePlayer.getFirstName();
    String lastName = gamePlayer.getLastName();
    Player player = Player.builder()
      .firstName(firstName)
      .lastName(lastName)
      .email(gamePlayer.getEmail())
      .roles(ImmutableSet.of(Role.builder()
        .type(Role.Type.USER)
        .build()))
      .build();
    int playerId = getPlayerModule().create(player).getId();
    gamePlayer.setPlayerId(playerId);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void updateGamePlayer(GamePlayer gamePlayer) {
    Game game = get(gamePlayer.getGameId());
    checkFinalized(game);

    GamePlayer existingGamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayer.getId())
      .findFirst().get();

    existingGamePlayer.setPlace(gamePlayer.getPlace());
    existingGamePlayer.setRoundUpdates(gamePlayer.getRoundUpdates());
    existingGamePlayer.setBuyInCollected(gamePlayer.getBuyInCollected());
    existingGamePlayer.setRebuyAddOnCollected(gamePlayer.getRebuyAddOnCollected());
    existingGamePlayer.setAnnualTocCollected(gamePlayer.getAnnualTocCollected());
    existingGamePlayer.setQuarterlyTocCollected(gamePlayer.getQuarterlyTocCollected());
    existingGamePlayer.setChop(gamePlayer.getChop());

    if (gamePlayer.getPlace() != null && gamePlayer.getPlace() <= 10) {
      gamePlayer.setKnockedOut(true);
    } else {
      gamePlayer.setKnockedOut(gamePlayer.getKnockedOut());
    }

    gameRepository.save(game);

    recalculate(game);
    sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    Game game = get(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayerId)
      .findFirst().get();
    Boolean knockedOut = gamePlayer.getKnockedOut();
    if (knockedOut == null) {
      knockedOut = true;
    } else {
      knockedOut = !knockedOut;
    }
    gamePlayer.setKnockedOut(knockedOut);

    gameRepository.save(game);

    recalculate(game);
    sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    Game game = get(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayerId)
      .findFirst().get();
    Integer rebuy = gamePlayer.getRebuyAddOnCollected();
    if (rebuy == null || rebuy != game.getRebuyAddOnCost()) {
      rebuy = game.getRebuyAddOnCost();
    } else {
      rebuy = null;
    }
    gamePlayer.setRebuyAddOnCollected(rebuy);

    gameRepository.save(game);

    recalculate(game);
    sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    Game game = get(gameId);
    checkFinalized(game);

    GamePlayer gamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayerId)
      .findFirst().get();
    game.getPlayers().remove(gamePlayer);
    gameRepository.save(game);

    recalculate(game);
    sendUpdatedGame();
  }

//  @Transactional(readOnly = true)
//  public GamePlayer getGamePlayer(int gamePlayerId) {
//    return gamePlayerRepository.selectById(gamePlayerId);
//  }

  @CacheEvict(value = {"currentGame", "currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void finalize(int id) {
    // TODO check that the game has the appropriate finishes (e.g. 1st, 2nd, ...)
    Game game = get(id);
    recalculate(game);
    game = get(id);
    game.setFinalized(true);
    game.setSeating(null);
    // TODO set game.chopped
    gameRepository.save(game);
    // TODO message season for it to recalculate season and quarter
    sendUpdatedGame();
    // TODO message clock to end
    sendSummary(id);
  }

  @CacheEvict(value = {"currentGame", "currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  public void unfinalize(int id) {
    // TODO admin only
    Game gameToOpen = get(id);

    Season season = getSeasonModule().getSeasonById(gameToOpen.getSeasonId());
    if (season.isFinalized()) {
      // TODO throw exception and handle in RestControllerAdvise
      throw new RuntimeException("Cannot open a game when season is finalized");
    }

    // Make sure no other game is open
    List<Game> games = gameRepository.findBySeasonId(gameToOpen.getSeasonId());
    for (Game game : games) {
      if (game.getId() == gameToOpen.getId()) {
        continue;
      }
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    gameToOpen.setFinalized(false);
    gameRepository.save(gameToOpen);
    sendUpdatedGame();
  }

  // TODO pulled this out of the endGame method because there seems to be
  // a transactional problem
  public void sendSummary(int gameId) {
    sendGameSummary(gameId);
  }

  // Worker to avoid one @Transacation calling anther @Transactional
  private GamePlayer createGamePlayerWorker(GamePlayer gamePlayer, Game game) {
    if (gamePlayer.getFirstName() == null && gamePlayer.getLastName() == null) {
      Player player = getPlayerModule().get(gamePlayer.getPlayerId());
      gamePlayer.setFirstName(player.getFirstName());
      gamePlayer.setLastName(player.getLastName());
    }
    gamePlayer.setQSeasonId(game.getQSeasonId());
    gamePlayer.setSeasonId(game.getSeasonId());

    game.getPlayers().add(gamePlayer);
    // TODO verify the game player id gets set by spring data jdbc
    gameRepository.save(game);
    recalculate(game);
    return gamePlayer;
  }

  // TODO separate thread
  private void recalculate(Game game) {
    Game calculatedGame = gameCalculator.calculate(game);
    payoutCalculator.calculate(calculatedGame);
    pointsCalculator.calculate(calculatedGame);
  }

  private void checkFinalized(int id) {
    checkFinalized(get(id));
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

    Season season = getSeasonModule().getSeasonById(game.getSeasonId());
    context.put("season", season);

    QuarterlySeason currentQSeason = seasonModule.getQuarterlySeasonByDate(game.getDate());
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
      Game game = get(gameId);

      String body = getGameSummaryFromTemplate(game);
      String subject = "Summary " + game.getDate();

      for (Player player : getPlayerModule().getAll()) {
        boolean isAdmin = false;
        for (Role role : player.getRoles()) {
          if (Role.Type.ADMIN == role.getType()) {
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

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

  private SeasonModule getSeasonModule() {
    if (seasonModule == null) {
      seasonModule = SeasonModuleFactory.getSeasonModule();
    }
    return seasonModule;
  }

  private class GameSender implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      webSocketConnector.sendGame(getCurrent());
      return null;
    }
  }
}
