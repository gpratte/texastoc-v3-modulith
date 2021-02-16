package com.texastoc.module.quarterly.calculator;

import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.model.QuarterlySeasonPayout;
import com.texastoc.module.quarterly.model.QuarterlySeasonPlayer;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuarterlySeasonCalculator {

  private final QuarterlySeasonRepository qSeasonRepository;

  private GameModule gameModule;

  public QuarterlySeasonCalculator(QuarterlySeasonRepository qSeasonRepository) {
    this.qSeasonRepository = qSeasonRepository;
  }

  public void calculate(int id) {
    QuarterlySeason qSeason = qSeasonRepository.findById(id).get();

    // Calculate quarterly season
    List<Game> games = getGameModule().getByQuarterlySeasonId(id);

    qSeason.setNumGamesPlayed(games.size());

    int qTocCollected = 0;
    for (Game game : games) {
      qTocCollected += game.getQuarterlyTocCollected();
    }
    qSeason.setQTocCollected(qTocCollected);
    qSeason.setLastCalculated(LocalDateTime.now());

    // Calculate quarterly season players
    List<QuarterlySeasonPlayer> players = calculatePlayers(qSeason.getSeasonId(), id);
    qSeason.setPlayers(players);

    // Calculate quarterly season payouts
    List<QuarterlySeasonPayout> payouts = calculatePayouts(qTocCollected, qSeason.getSeasonId(),
        id);
    qSeason.setPayouts(payouts);

    // Persist quarterly season
    qSeasonRepository.save(qSeason);
  }

  private List<QuarterlySeasonPlayer> calculatePlayers(int seasonId, int qSeasonId) {
    Map<Integer, QuarterlySeasonPlayer> seasonPlayerMap = new HashMap<>();

    // TODO figure this out
//    List<GamePlayer> gamePlayers = gamePlayerRepository.selectQuarterlyTocPlayersByQuarterlySeasonId(qSeasonId);
//
//    for (GamePlayer gamePlayer : gamePlayers) {
//      QuarterlySeasonPlayer player = seasonPlayerMap.get(gamePlayer.getPlayerId());
//      if (player == null) {
//        player = QuarterlySeasonPlayer.builder()
//          .playerId(gamePlayer.getPlayerId())
//          .seasonId(seasonId)
//          .qSeasonId(qSeasonId)
//          .name(gamePlayer.getName())
//          .build();
//        seasonPlayerMap.put(gamePlayer.getPlayerId(), player);
//      }
//
//      if (gamePlayer.getPoints() != null && gamePlayer.getPoints() > 0) {
//        player.setPoints(player.getPoints() + gamePlayer.getPoints());
//      }
//
//      player.setEntries(player.getEntries() + 1);
//    }
//
//    List<QuarterlySeasonPlayer> players = new ArrayList<>(seasonPlayerMap.values());
//    Collections.sort(players);
//
//    int place = 0;
//    int lastPoints = -1;
//    int numTied = 0;
//    for (QuarterlySeasonPlayer player : players) {
//      if (player.getPoints() > 0) {
//        // check for a tie
//        if (player.getPoints() == lastPoints) {
//          // tie for points so same player
//          player.setPlace(place);
//          ++numTied;
//        } else {
//          place = ++place + numTied;
//          player.setPlace(place);
//          lastPoints = player.getPoints();
//          numTied = 0;
//        }
//      }
//    }
//
//    return players;
    return null;
  }

  private List<QuarterlySeasonPayout> calculatePayouts(int pot, int seasonId, int qSeasonId) {
    List<QuarterlySeasonPayout> payouts = new ArrayList<>(3);

    if (pot < 1) {
      return payouts;
    }

    int firstPlace = (int) Math.round(pot * 0.5d);
    int secondPlace = (int) Math.round(pot * 0.3d);
    int thirdPlace = pot - firstPlace - secondPlace;

    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(1)
        .amount(firstPlace)
        .build());
    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(2)
        .amount(secondPlace)
        .build());
    payouts.add(QuarterlySeasonPayout.builder()
        .seasonId(seasonId)
        .qSeasonId(qSeasonId)
        .place(3)
        .amount(thirdPlace)
        .build());

    return payouts;
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }
}