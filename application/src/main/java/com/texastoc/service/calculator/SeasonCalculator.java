package com.texastoc.service.calculator;

import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.season.*;
import com.texastoc.model.season.SeasonPayoutRange.SeasonPayoutPlace;
import com.texastoc.repository.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class SeasonCalculator {

  private final GameRepository gameRepository;
  private final SeasonRepository seasonRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final SeasonPlayerRepository seasonPlayerRepository;
  private final SeasonPayoutRepository seasonPayoutRepository;
  private final SeasonPayoutSettingsRepository seasonPayoutSettingsRepository;

  public SeasonCalculator(GameRepository gameRepository, SeasonRepository seasonRepository, SeasonPlayerRepository seasonPlayerRepository, GamePlayerRepository gamePlayerRepository, SeasonPayoutRepository seasonPayoutRepository, SeasonPayoutSettingsRepository seasonPayoutSettingsRepository) {
    this.gameRepository = gameRepository;
    this.seasonRepository = seasonRepository;
    this.seasonPlayerRepository = seasonPlayerRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.seasonPayoutRepository = seasonPayoutRepository;
    this.seasonPayoutSettingsRepository = seasonPayoutSettingsRepository;
  }

  public Season calculate(int id) {

    Season season = seasonRepository.get(id);

    // Calculate season
    List<Game> games = gameRepository.getBySeasonId(id);

    season.setNumGamesPlayed(games.size());

    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int totalCollected = 0;

    int annualTocFromRebuyAddOnCalculated = 0;
    int rebuyAddOnLessAnnualTocCalculated = 0;
    int totalCombinedAnnualTocCalculated = 0;
    int kittyCalculated = 0;
    int prizePotCalculated = 0;

    for (Game game : games) {
      buyInCollected += game.getBuyInCollected();
      rebuyAddOnCollected += game.getRebuyAddOnCollected();
      annualTocCollected += game.getAnnualTocCollected();
      totalCollected += game.getTotalCollected();

      annualTocFromRebuyAddOnCalculated += game.getAnnualTocFromRebuyAddOnCalculated();
      rebuyAddOnLessAnnualTocCalculated += game.getRebuyAddOnLessAnnualTocCalculated();
      totalCombinedAnnualTocCalculated += game.getAnnualTocCollected() + game.getAnnualTocFromRebuyAddOnCalculated();
      kittyCalculated += game.getKittyCalculated();
      prizePotCalculated += game.getPrizePotCalculated();
    }

    season.setBuyInCollected(buyInCollected);
    season.setRebuyAddOnCollected(rebuyAddOnCollected);
    season.setAnnualTocCollected(annualTocCollected);
    season.setTotalCollected(totalCollected);

    season.setAnnualTocFromRebuyAddOnCalculated(annualTocFromRebuyAddOnCalculated);
    season.setRebuyAddOnLessAnnualTocCalculated(rebuyAddOnLessAnnualTocCalculated);
    season.setTotalCombinedAnnualTocCalculated(totalCombinedAnnualTocCalculated);
    season.setKittyCalculated(kittyCalculated);
    season.setPrizePotCalculated(prizePotCalculated);

    season.setLastCalculated(LocalDateTime.now());

    // Persist season
    seasonRepository.update(season);

    // Calculate season players
    List<SeasonPlayer> players = calculatePlayers(id);
    season.setPlayers(players);

    // Persist season players
    seasonPlayerRepository.deleteBySeasonId(id);
    for (SeasonPlayer player : players) {
      seasonPlayerRepository.save(player);
    }

    // Calculate season current and estimated payouts
    calculatSeasonPayouts(season);

    // Persist season payouts
    seasonPayoutRepository.deleteBySeasonId(id);
    if (season.getPayouts() != null) {
      for (SeasonPayout payout : season.getPayouts()) {
        seasonPayoutRepository.save(payout);
      }
    }
    if (season.getEstimatedPayouts() != null) {
      for (SeasonPayout payout : season.getEstimatedPayouts()) {
        seasonPayoutRepository.save(payout);
      }
    }

    return season;
  }

  @SuppressWarnings("Duplicates")
  private List<SeasonPlayer> calculatePlayers(int id) {

    Map<Integer, SeasonPlayer> seasonPlayerMap = new HashMap<>();

    List<GamePlayer> gamePlayers = gamePlayerRepository.selectAnnualTocPlayersBySeasonId(id);
    for (GamePlayer gamePlayer : gamePlayers) {
      SeasonPlayer seasonPlayer = seasonPlayerMap.get(gamePlayer.getPlayerId());
      if (seasonPlayer == null) {
        seasonPlayer = SeasonPlayer.builder()
          .playerId(gamePlayer.getPlayerId())
          .seasonId(id)
          .name(gamePlayer.getName())
          .build();
        seasonPlayerMap.put(gamePlayer.getPlayerId(), seasonPlayer);
      }

      if (gamePlayer.getPoints() != null && gamePlayer.getPoints() > 0) {
        seasonPlayer.setPoints(seasonPlayer.getPoints() + gamePlayer.getPoints());
      }

      seasonPlayer.setEntries(seasonPlayer.getEntries() + 1);
    }

    List<SeasonPlayer> seasonPlayers = new ArrayList<>(seasonPlayerMap.values());
    Collections.sort(seasonPlayers);

    int place = 0;
    int lastPoints = -1;
    int numTied = 0;
    for (SeasonPlayer player : seasonPlayers) {
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

    return seasonPlayers;
  }

  private void calculatSeasonPayouts(Season season) {
    SeasonPayoutSettings seasonPayoutSettings = seasonPayoutSettingsRepository.getBySeasonId(season.getId());

    if (season.getNumGamesPlayed() == season.getNumGames()) {
      season.setPayouts(calculatePayouts(season.getTotalCombinedAnnualTocCalculated(), season.getId(), false, seasonPayoutSettings));
    } else {
      // Estimate the season TOC amount and payouts
      double seasonTocAmountPerGame = (double) season.getTotalCombinedAnnualTocCalculated() / (double) season.getNumGamesPlayed();
      int estimatedSeasonTocAmount = (int) (seasonTocAmountPerGame * season.getNumGames());
      season.setEstimatedPayouts(calculatePayouts(estimatedSeasonTocAmount, season.getId(), true, seasonPayoutSettings));
    }
  }

  private List<SeasonPayout> calculatePayouts(int seasonTocAmount, int seasonId, boolean estimated, SeasonPayoutSettings seasonPayoutSettings) {
    List<SeasonPayout> seasonPayouts = new LinkedList<>();

    List<SeasonPayoutRange> ranges = seasonPayoutSettings.getRanges();
    if (ranges == null || ranges.size() < 1) {
      return seasonPayouts;
    }

    for (SeasonPayoutRange range : ranges) {
      if (seasonTocAmount >= range.getLowRange() && seasonTocAmount < range.getHighRange()) {
        int amountToDivy = seasonTocAmount - range.getLowRange();
        for (SeasonPayoutPlace place : range.getGuaranteed()) {
          SeasonPayout seasonPayout = calculatePayout(place, amountToDivy);
          seasonPayout.setSeasonId(seasonId);
          seasonPayout.setGuarenteed(true);
          seasonPayout.setEstimated(estimated);
          seasonPayouts.add(seasonPayout);
        }
        for (SeasonPayoutPlace place : range.getFinalTable()) {
          SeasonPayout seasonPayout = calculatePayout(place, amountToDivy);
          seasonPayout.setSeasonId(seasonId);
          seasonPayout.setGuarenteed(false);
          seasonPayout.setEstimated(estimated);
          seasonPayouts.add(seasonPayout);
        }

        int amountOfPayouts = 0;
        for (SeasonPayout seasonPayout : seasonPayouts) {
          amountOfPayouts += seasonPayout.getAmount();
        }

        int amountYetToDivy = seasonTocAmount - amountOfPayouts;
        while (amountYetToDivy > 0) {
          for (SeasonPayout seasonPayout : seasonPayouts) {
            seasonPayout.setAmount(seasonPayout.getAmount() + 1);
            if (--amountYetToDivy == 0) {
              break;
            }
          }
        }
        break;
      }
    }

    return seasonPayouts;
  }

  private SeasonPayout calculatePayout(SeasonPayoutPlace place, int amountToDivy) {
    SeasonPayout seasonPayout = new SeasonPayout();
    seasonPayout.setPlace(place.getPlace());
    seasonPayout.setAmount(place.getAmount());
    if (place.getAmount() == 0) {
      seasonPayout.setCash(true);
    }
    if (amountToDivy > 0) {
      double payoutExtra = amountToDivy * (place.getPercent() / 100.0d);
      payoutExtra = Math.floor(payoutExtra);
      seasonPayout.setAmount(seasonPayout.getAmount() + (int) (payoutExtra));
    }
    return seasonPayout;
  }

}
