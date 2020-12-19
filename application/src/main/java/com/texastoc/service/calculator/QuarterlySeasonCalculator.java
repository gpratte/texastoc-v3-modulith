package com.texastoc.service.calculator;

import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.season.QuarterlySeason;
import com.texastoc.model.season.QuarterlySeasonPayout;
import com.texastoc.model.season.QuarterlySeasonPlayer;
import com.texastoc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class QuarterlySeasonCalculator {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final QuarterlySeasonRepository qSeasonRepository;
  private final QuarterlySeasonPlayerRepository qSeasonPlayerRepository;
  private final QuarterlySeasonPayoutRepository qSeasonPayoutRepository;

  public QuarterlySeasonCalculator(QuarterlySeasonRepository qSeasonRepository, GamePlayerRepository gamePlayerRepository, GameRepository gameRepository, QuarterlySeasonPlayerRepository qSeasonPlayerRepository, QuarterlySeasonPayoutRepository qSeasonPayoutRepository) {
    this.qSeasonRepository = qSeasonRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.gameRepository = gameRepository;
    this.qSeasonPlayerRepository = qSeasonPlayerRepository;
    this.qSeasonPayoutRepository = qSeasonPayoutRepository;
  }

  public QuarterlySeason calculate(int id) {
    QuarterlySeason qSeason = qSeasonRepository.getById(id);

    // Calculate quarterly season
    List<Game> games = gameRepository.getByQuarterlySeasonId(id);

    qSeason.setNumGamesPlayed(games.size());

    int qTocCollected = 0;
    for (Game game : games) {
      qTocCollected += game.getQuarterlyTocCollected();
    }
    qSeason.setQTocCollected(qTocCollected);
    qSeason.setLastCalculated(LocalDateTime.now());

    // Persist quarterly season
    qSeasonRepository.update(qSeason);


    // Calculate quarterly season players
    List<QuarterlySeasonPlayer> players = calculatePlayers(qSeason.getSeasonId(), id);
    qSeason.setPlayers(players);

    // Persist quarterly season players
    qSeasonPlayerRepository.deleteByQSeasonId(id);
    for (QuarterlySeasonPlayer player : players) {
      qSeasonPlayerRepository.save(player);
    }

    // Calculate quarterly season payouts
    List<QuarterlySeasonPayout> payouts = calculatePayouts(qTocCollected, qSeason.getSeasonId(), id);
    qSeason.setPayouts(payouts);

    // Persist quarterly season payouts
    qSeasonPayoutRepository.deleteByQSeasonId(id);
    for (QuarterlySeasonPayout payout : payouts) {
      qSeasonPayoutRepository.save(payout);
    }

    return qSeason;
  }

  @SuppressWarnings("Duplicates")
  private List<QuarterlySeasonPlayer> calculatePlayers(int seasonId, int qSeasonId) {

    Map<Integer, QuarterlySeasonPlayer> seasonPlayerMap = new HashMap<>();

    List<GamePlayer> gamePlayers = gamePlayerRepository.selectQuarterlyTocPlayersByQuarterlySeasonId(qSeasonId);

    for (GamePlayer gamePlayer : gamePlayers) {
      QuarterlySeasonPlayer player = seasonPlayerMap.get(gamePlayer.getPlayerId());
      if (player == null) {
        player = QuarterlySeasonPlayer.builder()
          .playerId(gamePlayer.getPlayerId())
          .seasonId(seasonId)
          .qSeasonId(qSeasonId)
          .name(gamePlayer.getName())
          .build();
        seasonPlayerMap.put(gamePlayer.getPlayerId(), player);
      }

      if (gamePlayer.getPoints() != null && gamePlayer.getPoints() > 0) {
        player.setPoints(player.getPoints() + gamePlayer.getPoints());
      }

      player.setEntries(player.getEntries() + 1);
    }

    List<QuarterlySeasonPlayer> players = new ArrayList<>(seasonPlayerMap.values());
    Collections.sort(players);

    int place = 0;
    int lastPoints = -1;
    int numTied = 0;
    for (QuarterlySeasonPlayer player : players) {
      if (player.getPoints() > 0) {
        // check for a tie
        if (player.getPoints() == lastPoints) {
          // tie for points so same player
          player.setPlace(place);
          ++numTied;
        } else {
          place = ++place + numTied;
          player.setPlace(place);
          lastPoints = player.getPoints();
          numTied = 0;
        }
      }
    }

    return players;
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
}