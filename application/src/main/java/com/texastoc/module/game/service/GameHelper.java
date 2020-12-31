package com.texastoc.module.game.service;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.SeasonModuleFactory;
import com.texastoc.module.season.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
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
public class GameHelper {

  private final GameRepository gameRepository;
  private final GameCalculator gameCalculator;
  private final PayoutCalculator payoutCalculator;
  private final PointsCalculator pointsCalculator;
  private final ExecutorService executorService;

  // TODO move to the notification module
  private final EmailConnector emailConnector;
  private final WebSocketConnector webSocketConnector;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;

  public GameHelper(GameRepository gameRepository, GameCalculator gameCalculator, PayoutCalculator payoutCalculator, PointsCalculator pointsCalculator, EmailConnector emailConnector, WebSocketConnector webSocketConnector) {
    this.gameRepository = gameRepository;
    this.gameCalculator = gameCalculator;
    this.payoutCalculator = payoutCalculator;
    this.pointsCalculator = pointsCalculator;
    this.emailConnector = emailConnector;
    this.webSocketConnector = webSocketConnector;

    executorService = Executors.newCachedThreadPool();
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    Optional<Game> optionalGame = gameRepository.findById(id);
    if (!optionalGame.isPresent()) {
      throw new NotFoundException("Game with id " + id + " not found");
    }
    return optionalGame.get();
  }

  public Game getCurrent() {
    int seasonId = getSeasonModule().getCurrentSeasonId();
    List<Game> games = gameRepository.findUnfinalizedBySeasonId(seasonId);
    if (games.size() > 0) {
      return games.get(0);
    }

    games = gameRepository.findMostRecentBySeasonId(seasonId);
    if (games.size() > 0) {
      return games.get(0);
    }

    throw new NotFoundException("Current game not found");
  }

  // TODO pulled this out of the endGame method because there seems to be
  // a transactional problem
  public void sendSummary(int gameId) {
    sendGameSummary(gameId);
  }

  // TODO separate thread
  public void recalculate(Game game) {
    Game calculatedGame = gameCalculator.calculate(game);
    payoutCalculator.calculate(calculatedGame);
    pointsCalculator.calculate(calculatedGame);
  }

  public void checkFinalized(Game game) {
    if (game.isFinalized()) {
      throw new GameIsFinalizedException("Game is finalized");
    }
  }

  // TODO move to notification module
  public void sendUpdatedGame() {
    executorService.submit(new GameSender());
  }

  // TODO move to notification module
  private void sendGameSummary(int id) {
    SendGameSummary sgs = new SendGameSummary(id);
    new Thread(sgs).start();
  }

  // TODO move to notification module
  private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();

  static {
    VELOCITY_ENGINE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    VELOCITY_ENGINE.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    VELOCITY_ENGINE.init();
  }

  // TODO move to notification module
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

  // TODO move to notification module
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

  // TODO move to notification module
  private class GameSender implements Callable<Void> {
    @Override
    public Void call() throws Exception {
      webSocketConnector.sendGame(getCurrent());
      return null;
    }
  }
}
