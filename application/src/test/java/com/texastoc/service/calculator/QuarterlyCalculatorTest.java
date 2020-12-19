package com.texastoc.service.calculator;

import com.texastoc.TestConstants;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.season.QuarterlySeason;
import com.texastoc.model.season.QuarterlySeasonPayout;
import com.texastoc.model.season.QuarterlySeasonPlayer;
import com.texastoc.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
public class QuarterlyCalculatorTest implements TestConstants {

  private QuarterlySeasonCalculator qSeasonCalculator;
  private GameCalculator gameCalculator;

  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private GameRepository gameRepository;
  @MockBean
  private GamePlayerRepository gamePlayerRepository;
  @MockBean
  private ConfigRepository configRepository;
  @MockBean
  private QuarterlySeasonRepository qSeasonRepository;
  @MockBean
  private QuarterlySeasonPlayerRepository qSeasonPlayerRepository;
  @MockBean
  private QuarterlySeasonPayoutRepository qSeasonPayoutRepository;

  @Before
  public void before() {
    qSeasonCalculator = new QuarterlySeasonCalculator(qSeasonRepository, gamePlayerRepository, gameRepository, qSeasonPlayerRepository, qSeasonPayoutRepository);
    gameCalculator = new GameCalculator(gameRepository, configRepository);
  }

  @Test
  public void testNoGames() {

    QuarterlySeason currentSeason = QuarterlySeason.builder()
      .id(1)
      .build();
    Mockito.when(qSeasonRepository.getById(1)).thenReturn(currentSeason);

    Mockito.when(gamePlayerRepository.selectQuarterlyTocPlayersByQuarterlySeasonId(1)).thenReturn(Collections.emptyList());

    QuarterlySeason qSeason = qSeasonCalculator.calculate(1);

    Mockito.verify(qSeasonRepository, Mockito.times(1)).getById(1);
    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectQuarterlyTocPlayersByQuarterlySeasonId(1);

    Assert.assertNotNull("quarterly season not null", qSeason);
    Assert.assertEquals("quarterly season id 1", 1, (int) qSeason.getId());

    Assert.assertEquals("quarter has no games played", 0, qSeason.getNumGamesPlayed());
    Assert.assertEquals("qTocCollected is 0", 0, qSeason.getQTocCollected());

    Assert.assertEquals("players 0", 0, qSeason.getPlayers().size());
    Assert.assertEquals("payouts 0", 0, qSeason.getPayouts().size());
  }

  @Test
  public void test1Games() {

    QuarterlySeason currentSeason = QuarterlySeason.builder()
      .id(1)
      .build();
    Mockito.when(qSeasonRepository.getById(1)).thenReturn(currentSeason);

    List<GamePlayer> gameQSeasonPlayers = new ArrayList<>(10);
    List<Integer> expectedPoints = new ArrayList<>();
    for (int i = 0; i < 10; ++i) {
      int points = 0;
      if (i % 3 == 0 && i != 0) {
        points = i;
        expectedPoints.add(points);
      }

      GamePlayer gamePlayer = GamePlayer.builder()
        .id(i)
        .playerId(i)
        .gameId(1)
        .buyInCollected(GAME_BUY_IN)
        .rebuyAddOnCollected(GAME_REBUY)
        .annualTocCollected(TOC_PER_GAME)
        .quarterlyTocCollected(QUARTERLY_TOC_PER_GAME)
        .points(points)
        .build();
      gameQSeasonPlayers.add(gamePlayer);
    }

    List<GamePlayer> gameNonSeasonPlayers = new ArrayList<>(5);
    for (int i = 0; i < 5; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
        .id(i)
        .playerId(i)
        .buyInCollected(GAME_BUY_IN)
        .rebuyAddOnCollected(GAME_REBUY)
        .gameId(1)
        .build();
      gameNonSeasonPlayers.add(gamePlayer);
    }

    Mockito.when(gamePlayerRepository.selectQuarterlyTocPlayersByQuarterlySeasonId(1)).thenReturn(gameQSeasonPlayers);

    List<GamePlayer> gameCombinedPlayers = new ArrayList<>(15);
    gameCombinedPlayers.addAll(gameQSeasonPlayers);
    gameCombinedPlayers.addAll(gameNonSeasonPlayers);

    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());
    Game currentGame = Game.builder()
      .build();
    Game calculatedGame = gameCalculator.calculate(currentGame, gameCombinedPlayers);
    List<Game> calculatedGames = new LinkedList<>();
    calculatedGames.add(calculatedGame);
    Mockito.when(gameRepository.getByQuarterlySeasonId(1)).thenReturn(calculatedGames);

    int qTocCollected = 0;
    for (Game game : calculatedGames) {
      qTocCollected += game.getQuarterlyTocCollected();
    }

    QuarterlySeason qSeason = qSeasonCalculator.calculate(1);

    Mockito.verify(qSeasonRepository, Mockito.times(1)).getById(1);
    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectQuarterlyTocPlayersByQuarterlySeasonId(1);

    Assert.assertNotNull("quarterly season not null", qSeason);
    Assert.assertEquals("quarterly season id 1", 1, (int) qSeason.getId());

    Assert.assertEquals("quarter has 1 game played", 1, qSeason.getNumGamesPlayed());
    Assert.assertEquals("qTocCollected is " + qTocCollected, qTocCollected, qSeason.getQTocCollected());

    Assert.assertEquals("players 10", 10, qSeason.getPlayers().size());
    checkPoints(expectedPoints, qSeason.getPlayers());

    List<QuarterlySeasonPayout> payouts = qSeason.getPayouts();
    Assert.assertEquals("payouts " + QUARTERLY_NUM_PAYOUTS, QUARTERLY_NUM_PAYOUTS, payouts.size());

    int firstPlace = (int) Math.round(qTocCollected * 0.5d);
    int secondPlace = (int) Math.round(qTocCollected * 0.3d);
    int thirdPlace = qTocCollected - firstPlace - secondPlace;
    int amounts[] = {firstPlace, secondPlace, thirdPlace};

    for (int i = 0; i < 3; i++) {
      int place = i + 1;
      boolean found = false;
      for (QuarterlySeasonPayout payout : payouts) {
        if (payout.getPlace() == place) {
          found = true;
          Assert.assertEquals("payout " + place + " should be " + amounts[i], amounts[i], payout.getAmount());
        }
      }
      Assert.assertTrue("should have found a payout for place " + place, found);
    }
  }

  private void checkPoints(Collection<Integer> expectedPoints, List<QuarterlySeasonPlayer> players) {
    for (int points : expectedPoints) {
      boolean found = false;
      for (QuarterlySeasonPlayer player : players) {
        if (points != 0 && player.getPoints() == points) {
          if (found) {
            Assert.fail("already found player with points " + points);
          } else {
            found = true;
          }
        }
      }
      Assert.assertTrue("should have found a player with points " + points, found);
    }
  }

}
