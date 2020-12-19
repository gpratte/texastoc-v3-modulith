package com.texastoc.service.calculator;

import com.texastoc.TestConstants;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.season.Season;
import com.texastoc.model.season.SeasonPlayer;
import com.texastoc.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

@RunWith(SpringRunner.class)
public class SeasonCalculatorTest implements TestConstants {

  private GameCalculator gameCalculator;
  private SeasonCalculator seasonCalculator;

  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private GameRepository gameRepository;
  @MockBean
  private ConfigRepository configRepository;
  @MockBean
  private SeasonRepository seasonRepository;
  @MockBean
  private SeasonPlayerRepository seasonPlayerRepository;
  @MockBean
  private GamePlayerRepository gamePlayerRepository;
  @MockBean
  private SeasonPayoutRepository seasonPayoutRepository;
  @MockBean
  private SeasonPayoutSettingsRepository seasonPayoutSettingsRepository;


  @Before
  public void before() {
    seasonCalculator = new SeasonCalculator(gameRepository, seasonRepository, seasonPlayerRepository, gamePlayerRepository, seasonPayoutRepository, seasonPayoutSettingsRepository);
    gameCalculator = new GameCalculator(gameRepository, configRepository);
  }

  @Ignore
  @Test
  public void testNoGames() {

    Season currentSeason = Season.builder()
      .id(1)
      .build();
    Mockito.when(seasonRepository.get(1)).thenReturn(currentSeason);

    Mockito.when(gameRepository.getBySeasonId(1)).thenReturn(Collections.emptyList());

    Season season = seasonCalculator.calculate(1);

    Assert.assertNotNull("season returned from calculator should not be null", season);

    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
    Mockito.verify(gameRepository, Mockito.times(1)).getBySeasonId(1);

    Assert.assertEquals("season id should be 1", 1, (int) season.getId());
    Assert.assertEquals("numGamesPlayed should be 0", 0, (int) season.getNumGamesPlayed());
    Assert.assertEquals("buyInCollected should be 0", 0, (int) season.getBuyInCollected());
    Assert.assertEquals("rebuyAddOnCollected should be 0", 0, (int) season.getRebuyAddOnCollected());
    Assert.assertEquals("annualTocCollected should be 0", 0, (int) season.getAnnualTocCollected());
    Assert.assertEquals("totalCollected should be 0", 0, (int) season.getTotalCollected());

    Assert.assertEquals("annualTocFromRebuyAddOnCalculated should be 0", 0, (int) season.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuyAddOnLessAnnualTocCalculated should be 0", 0, (int) season.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("totalCombinedAnnualTocCalculated should be 0", 0, (int) season.getTotalCombinedAnnualTocCalculated());
    Assert.assertEquals("kittyCalculated should be 0", 0, (int) season.getKittyCalculated());
    Assert.assertEquals("prizePotCalculated should be 0", 0, (int) season.getPrizePotCalculated());

    Assert.assertEquals("numGamesPlayed should be 0", 0, (int) season.getNumGamesPlayed());

    Assert.assertTrue("last calculated should be within the last few seconds", season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    Assert.assertEquals("players 0", 0, season.getPlayers().size());
    Assert.assertEquals("payouts 0", 0, season.getPayouts().size());
  }

  @Ignore
  @Test
  public void test1Game() {

    Season currentSeason = Season.builder()
      .id(1)
      .build();
    Mockito.when(seasonRepository.get(1)).thenReturn(currentSeason);

    List<GamePlayer> gameSeasonPlayers = new ArrayList<>(10);
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
      gameSeasonPlayers.add(gamePlayer);
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

    List<GamePlayer> gameCombinedPlayers = new ArrayList<>(15);
    gameCombinedPlayers.addAll(gameSeasonPlayers);
    gameCombinedPlayers.addAll(gameNonSeasonPlayers);

    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());
    Game currentGame = Game.builder()
      .build();
    Game calculatedGame = gameCalculator.calculate(currentGame, gameCombinedPlayers);
    List<Game> calculatedGames = new LinkedList<>();
    calculatedGames.add(calculatedGame);

    Mockito.when(gameRepository.getBySeasonId(1)).thenReturn(calculatedGames);
    Mockito.when(gamePlayerRepository.selectAnnualTocPlayersBySeasonId(1)).thenReturn(gameSeasonPlayers);

    Season season = seasonCalculator.calculate(1);

    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
    Mockito.verify(gameRepository, Mockito.times(1)).getBySeasonId(1);

    Assert.assertNotNull("season returned from calculator should not be null", season);
    Assert.assertEquals("season id should be 1", 1, (int) season.getId());
    Assert.assertEquals("numGamesPlayed should be 1", 1, (int) season.getNumGamesPlayed());

    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int totalCollected = 0;
    int annualTocFromRebuyAddOnCalculated = 0;
    int rebuyAddOnLessAnnualTocCalculated = 0;
    int totalCombinedAnnualTocCalculated = 0;
    int kittyCalculated = 0;
    int prizePotCalculated = 0;

    for (Game game : calculatedGames) {
      buyInCollected += game.getBuyInCollected();
      rebuyAddOnCollected += game.getRebuyAddOnCollected();
      annualTocCollected += game.getAnnualTocCollected();
      totalCollected += game.getTotalCollected();

      annualTocFromRebuyAddOnCalculated += game.getAnnualTocFromRebuyAddOnCalculated();
      rebuyAddOnLessAnnualTocCalculated += game.getRebuyAddOnLessAnnualTocCalculated();
      totalCombinedAnnualTocCalculated += game.getTotalCombinedTocCalculated();
      kittyCalculated += game.getKittyCalculated();
      prizePotCalculated += game.getPrizePotCalculated();
    }

    Assert.assertEquals("buyInCollected should be " + buyInCollected, buyInCollected, season.getBuyInCollected());
    Assert.assertEquals("rebuyAddOnCollected should be " + rebuyAddOnCollected, rebuyAddOnCollected, season.getRebuyAddOnCollected());
    Assert.assertEquals("annualTocCollected should be " + annualTocCollected, annualTocCollected, season.getAnnualTocCollected());
    Assert.assertEquals("totalCollected should be " + totalCollected, totalCollected, season.getTotalCollected());

    Assert.assertEquals("annualTocFromRebuyAddOnCalculated should be " + annualTocFromRebuyAddOnCalculated, annualTocFromRebuyAddOnCalculated, season.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuyAddOnLessAnnualTocCalculated should be " + rebuyAddOnLessAnnualTocCalculated, rebuyAddOnLessAnnualTocCalculated, season.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("totalCombinedAnnualTocCalculated should be " + totalCombinedAnnualTocCalculated, totalCombinedAnnualTocCalculated, season.getTotalCombinedAnnualTocCalculated());
    Assert.assertEquals("kittyCalculated should be " + kittyCalculated, kittyCalculated, season.getKittyCalculated());
    Assert.assertEquals("prizePotCalculated should be " + prizePotCalculated, prizePotCalculated, season.getPrizePotCalculated());

    Assert.assertTrue("last calculated should be within the last few seconds", season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    Assert.assertEquals("season players should be 10", 10, season.getPlayers().size());
    checkPoints(expectedPoints, season.getPlayers());

    //        Assert.assertEquals("payouts 0", 0, season.getPayouts().size());
  }

  @Ignore
  @Test
  public void test2Games() {

    Season currentSeason = Season.builder()
      .id(1)
      .build();
    Mockito.when(seasonRepository.get(1)).thenReturn(currentSeason);

    Map<Integer, Integer> expectedPoints = new HashMap<>();

    List<GamePlayer> game1SeasonPlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      int points = 0;
      if (i % 2 == 0 && i != 0) {
        expectedPoints.put(i, i);
        points = i;
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
      game1SeasonPlayers.add(gamePlayer);
    }

    List<GamePlayer> game1NonSeasonPlayers = new ArrayList<>(5);
    for (int i = 0; i < 5; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
        .id(i)
        .playerId(i)
        .buyInCollected(GAME_BUY_IN)
        .rebuyAddOnCollected(GAME_REBUY)
        .gameId(1)
        .build();
      game1NonSeasonPlayers.add(gamePlayer);
    }

    // Another game
    List<GamePlayer> game2SeasonPlayers = new ArrayList<>(10);
    for (int i = 0; i < 10; ++i) {
      int points = 0;
      if (i % 4 == 0 && i != 0) {
        Integer currentPoints = expectedPoints.get(i);
        if (currentPoints == null) {
          expectedPoints.put(i, i);
        } else {
          expectedPoints.put(i, i + currentPoints);
        }
        points = i;
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
      game2SeasonPlayers.add(gamePlayer);
    }

    // another game
    List<GamePlayer> game2NonSeasonPlayers = new ArrayList<>(5);
    for (int i = 0; i < 2; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
        .id(i)
        .playerId(i)
        .buyInCollected(GAME_BUY_IN)
        .rebuyAddOnCollected(GAME_REBUY)
        .gameId(2)
        .build();
      game2NonSeasonPlayers.add(gamePlayer);
    }

    List<GamePlayer> game1CombinedPlayers = new ArrayList<>();
    game1CombinedPlayers.addAll(game1SeasonPlayers);
    game1CombinedPlayers.addAll(game1NonSeasonPlayers);

    List<GamePlayer> game2CombinedPlayers = new ArrayList<>();
    game2CombinedPlayers.addAll(game2SeasonPlayers);
    game2CombinedPlayers.addAll(game2NonSeasonPlayers);

    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());

    Game currentGame = Game.builder()
      .build();
    Game calculatedGame1 = gameCalculator.calculate(currentGame, game1CombinedPlayers);
    List<Game> calculatedGames = new LinkedList<>();
    calculatedGames.add(calculatedGame1);

    Game calculatedGame2 = gameCalculator.calculate(currentGame, game2CombinedPlayers);
    calculatedGames.add(calculatedGame2);

    Mockito.when(gameRepository.getBySeasonId(1)).thenReturn(calculatedGames);

    List<GamePlayer> game1And2Players = new ArrayList<>();
    game1And2Players.addAll(game1SeasonPlayers);
    game1And2Players.addAll(game2SeasonPlayers);
    Mockito.when(gamePlayerRepository.selectAnnualTocPlayersBySeasonId(1)).thenReturn(game1And2Players);

    Season season = seasonCalculator.calculate(1);

    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
    Mockito.verify(gameRepository, Mockito.times(1)).getBySeasonId(1);

    Assert.assertNotNull("season returned from calculator should not be null", season);
    Assert.assertEquals("season id should be 1", 1, (int) season.getId());
    Assert.assertEquals("numGamesPlayed should be 2", 2, (int) season.getNumGamesPlayed());

    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int totalCollected = 0;
    int annualTocFromRebuyAddOnCalculated = 0;
    int rebuyAddOnLessAnnualTocCalculated = 0;
    int totalCombinedAnnualTocCalculated = 0;
    int kittyCalculated = 0;
    int prizePotCalculated = 0;

    for (Game game : calculatedGames) {
      buyInCollected += game.getBuyInCollected();
      rebuyAddOnCollected += game.getRebuyAddOnCollected();
      annualTocCollected += game.getAnnualTocCollected();
      totalCollected += game.getTotalCollected();

      annualTocFromRebuyAddOnCalculated += game.getAnnualTocFromRebuyAddOnCalculated();
      rebuyAddOnLessAnnualTocCalculated += game.getRebuyAddOnLessAnnualTocCalculated();
      totalCombinedAnnualTocCalculated += game.getTotalCombinedTocCalculated();
      kittyCalculated += game.getKittyCalculated();
      prizePotCalculated += game.getPrizePotCalculated();
    }

    Assert.assertEquals("buyInCollected should be " + buyInCollected, buyInCollected, season.getBuyInCollected());
    Assert.assertEquals("rebuyAddOnCollected should be " + rebuyAddOnCollected, rebuyAddOnCollected, season.getRebuyAddOnCollected());
    Assert.assertEquals("annualTocCollected should be " + annualTocCollected, annualTocCollected, season.getAnnualTocCollected());
    Assert.assertEquals("totalCollected should be " + totalCollected, totalCollected, season.getTotalCollected());

    Assert.assertEquals("annualTocFromRebuyAddOnCalculated should be " + annualTocFromRebuyAddOnCalculated, annualTocFromRebuyAddOnCalculated, season.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuyAddOnLessAnnualTocCalculated should be " + rebuyAddOnLessAnnualTocCalculated, rebuyAddOnLessAnnualTocCalculated, season.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("totalCombinedAnnualTocCalculated should be " + totalCombinedAnnualTocCalculated, totalCombinedAnnualTocCalculated, season.getTotalCombinedAnnualTocCalculated());
    Assert.assertEquals("kittyCalculated should be " + kittyCalculated, kittyCalculated, season.getKittyCalculated());
    Assert.assertEquals("prizePotCalculated should be " + prizePotCalculated, prizePotCalculated, season.getPrizePotCalculated());

    Assert.assertTrue("last calculated should be within the last few seconds", season.getLastCalculated().isAfter(LocalDateTime.now().minusSeconds(3)));

    Assert.assertEquals("season players should be 10", 10, season.getPlayers().size());
    checkPoints(expectedPoints.values(), season.getPlayers());

    //        Assert.assertEquals("payouts 0", 0, season.getPayouts().size());
  }

  private void checkPoints(Collection<Integer> expectedPoints, List<SeasonPlayer> seasonPlayers) {
    for (int points : expectedPoints) {
      boolean found = false;
      for (SeasonPlayer seasonPlayer : seasonPlayers) {
        if (points != 0 && seasonPlayer.getPoints() == points) {
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
