package com.texastoc.module.game.service;

import com.google.common.collect.ImmutableList;
import com.texastoc.TestConstants;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.model.Quarter;
import com.texastoc.module.season.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GameServiceTest implements TestConstants {

  private GameService gameService;

  private GameRepository gameRepository;
  private GameHelper gameHelper;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameHelper = mock(GameHelper.class);
    playerModule = mock(PlayerModule.class);
    seasonModule = mock(SeasonModule.class);
    gameService = new GameService(gameRepository, gameHelper);
    ReflectionTestUtils.setField(gameService, "playerModule", playerModule);
    ReflectionTestUtils.setField(gameService, "seasonModule", seasonModule);
  }

  @Test
  public void testCreateGame() {
    // Arrange
    // Current season
    when(seasonModule.getCurrentSeason())
      .thenReturn(Season.builder()
        .id(16)
        .kittyPerGame(KITTY_PER_GAME)
        .tocPerGame(TOC_PER_GAME)
        .quarterlyTocPerGame(QUARTERLY_TOC_PER_GAME)
        .quarterlyNumPayouts(QUARTERLY_NUM_PAYOUTS)
        .buyInCost(GAME_BUY_IN)
        .rebuyAddOnCost(GAME_REBUY)
        .rebuyAddOnTocDebit(GAME_REBUY_TOC_DEBIT)
        .numGamesPlayed(20)
        .build());

    // Other games are finalized
    when(gameRepository.findBySeasonId(16))
      .thenReturn(ImmutableList.of(Game.builder()
        .finalized(true)
        .build()));

    // Quarterly season
    LocalDate gameDate = LocalDate.now();
    when(seasonModule.getQuarterlySeasonByDate(gameDate))
      .thenReturn(QuarterlySeason.builder()
        .id(17)
        .quarter(Quarter.FIRST)
        .seasonId(16)
        .numGamesPlayed(8)
        .build());

    // Host
    Mockito.when(playerModule.get(55))
      .thenReturn(Player.builder()
        .id(1)
        .firstName("Johnny")
        .lastName("Host The Most")
        .build());

    Game expected = Game.builder()
      .date(gameDate)
      .hostId(55)
      .transportRequired(true)
      .build();

    // Act
    gameService.create(expected);

    // Game repository called once
    Mockito.verify(gameRepository, Mockito.times(1)).save(any(Game.class));

    // Game argument match
    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());
    Game actual = gameArg.getValue();

    // Assert
    assertNotNull(actual);
    assertEquals("SeasonId should be 16", 16, actual.getSeasonId());
    assertEquals("QuarterlySeasonId should be 17", 17, actual.getQSeasonId());
    assertEquals("Host id should be 55", expected.getHostId(), actual.getHostId());
    assertEquals("date should be now", expected.getDate(), actual.getDate());

    assertTrue("Host name should be Johnny Host The Most", "Johnny Host The Most".equals(actual.getHostName()));
    assertEquals("Quarter should be first", Quarter.FIRST, actual.getQuarter());
    assertNull("last calculated should be null", actual.getLastCalculated());

    // Game setup variables
    assertEquals("transport required", expected.isTransportRequired(), actual.isTransportRequired());
    assertEquals("Kitty cost should be amount set for season", KITTY_PER_GAME, (int) actual.getKittyCost());
    assertEquals("Annual TOC be amount set for season", TOC_PER_GAME, (int) actual.getAnnualTocCost());
    assertEquals("Quarterly TOC be amount set for season", QUARTERLY_TOC_PER_GAME, (int) actual.getQuarterlyTocCost());

    // Game runtime variables
    assertNull("not started", actual.getStarted());

    assertEquals("No players", 0, (int) actual.getNumPlayers());
    assertEquals("No buy in collected", 0, (int) actual.getBuyInCollected());
    assertEquals("No rebuy collected", 0, (int) actual.getRebuyAddOnCollected());
    assertEquals("No annual toc collected", 0, (int) actual.getAnnualTocCollected());
    assertEquals("No quarterly toc collected", 0, (int) actual.getQuarterlyTocCollected());
    assertEquals("total collected", 0, (int) actual.getTotalCollected());

    assertEquals("no annualTocFromRebuyAddOnCalculated", 0, (int) actual.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals("no rebuyAddOnLessAnnualTocCalculated", 0, (int) actual.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals("no totalCombinedTocCalculated", 0, (int) actual.getTotalCombinedTocCalculated());
    assertEquals("No kitty calculated", 0, (int) actual.getKittyCalculated());
    assertEquals("no prizePotCalculated", 0, (int) actual.getPrizePotCalculated());

    Assert.assertFalse("not finalized", actual.isFinalized());

    assertEquals("Buy in cost should be amount set for season", GAME_BUY_IN, (int) actual.getBuyInCost());
    assertEquals("Rebuy cost should be amount set for season", GAME_REBUY, (int) actual.getRebuyAddOnCost());
    assertEquals("Rebuy Toc debit cost should be amount set for season", GAME_REBUY_TOC_DEBIT, (int) actual.getRebuyAddOnTocDebit());
  }

  @Test
  public void testUpdateGame() {
    Mockito.when(gameHelper.get(1)).thenReturn(Game.builder()
      .id(1)
      .hostId(0)
      .date(LocalDate.now().minusDays(1))
      .transportRequired(false)
      .finalized(false)
      .build());

    LocalDate now = LocalDate.now();
    Game expected = Game.builder()
      .id(1)
      .hostId(2)
      .date(now)
      .transportRequired(true)
      .build();
    gameService.update(expected);

    Mockito.verify(gameRepository, Mockito.times(1)).save(any(Game.class));
  }

  @Test
  public void testGetBySeasonId() {
    when(gameRepository.findBySeasonId(1))
      .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
        Game.builder().id(2).build()));

    List<Game> games = gameService.getBySeasonId(1);
    assertEquals("expect two games", 2, games.size());
  }

  @Test
  public void testGetByNoSeasonId() {
    when(seasonModule.getCurrentSeasonId()).thenReturn(1);

    when(gameRepository.findBySeasonId(1))
      .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
        Game.builder().id(2).build(),
        Game.builder().id(3).build()));

    List<Game> games = gameService.getBySeasonId(null);
    assertEquals("expect three games", 3, games.size());
  }

  @Test
  public void testGetByQuarterlySeasonId() {
    when(gameRepository.findByQuarterlySeasonId(1))
      .thenReturn(ImmutableList.of(Game.builder().id(1).build(),
        Game.builder().id(2).build()));

    List<Game> games = gameService.getByQuarterlySeasonId(1);
    assertEquals("expect two games", 2, games.size());
  }

//  @Test
//  public void testFinalize() {
//    Mockito.when(gameRepository.getById(1))
//      .thenReturn(Game.builder()
//        .id(1)
//        .qSeasonId(1)
//        .seasonId(1)
//        .build());
//
//    Mockito.doNothing().when(gameRepository).update((Game) notNull());
//
//    gameService.endGame(1);
//
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//    Mockito.verify(gameRepository, Mockito.times(1)).update(any(Game.class));
//    Mockito.verify(qSeasonCalculator, Mockito.times(1)).calculate(1);
//    Mockito.verify(seasonCalculator, Mockito.times(1)).calculate(1);
//    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
//  }
//
//  @Test
//  public void testFinalizeNoChanges() {
//    Mockito.when(gameRepository.getById(1))
//      .thenReturn(Game.builder()
//        .id(1)
//        .finalized(true)
//        .build());
//
//    try {
//      gameService.updateGame(Game.builder()
//        .id(1)
//        .build());
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameIsFinalizedException e) {
//      // all good
//    }
//
//    try {
//      gameService.createGamePlayer(1, CreateGamePlayerRequest.builder()
//        .build());
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameIsFinalizedException e) {
//      // all good
//    }
//
//    try {
//      gameService.updateGamePlayer(1, 1, UpdateGamePlayerRequest.builder().build());
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameIsFinalizedException e) {
//      // all good
//    }
//
//    Mockito.when(gamePlayerRepository.selectById(1)).thenReturn(GamePlayer.builder()
//      .id(1)
//      .gameId(1)
//      .build());
//
//    try {
//      gameService.deleteGamePlayer(1, 1);
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameIsFinalizedException e) {
//      // all good
//    }
//
//    try {
//      gameService.createFirstTimeGamePlayer(1, FirstTimeGamePlayer.builder()
//        .build());
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameIsFinalizedException e) {
//      // all good
//    }
//  }
//
//  @Ignore
//  @Test
//  public void testUnFinalizeNoNewGame() {
//
//    // Mocking needed to get current game begin >>>
//    Season season = Season.builder()
//      .id(1)
//      .build();
//    Mockito.when(seasonRepository.getCurrent())
//      .thenReturn(season);
//
//    List<Game> games = new LinkedList<>();
//    games.add(Game.builder()
//      .id(1)
//      .finalized(false)
//      .build());
//    Mockito.when(gameRepository.getMostRecent(1))
//      .thenReturn(games);
//
//    Mockito.when(gamePlayerRepository.selectByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Mockito.when(gamePayoutRepository.getByGameId(1))
//      .thenReturn(Collections.emptyList());
//    // <<< Mocking needed to get current game end
//
//    try {
//      gameService.createGame(Game.builder()
//        .id(1)
//        .build());
//      Assert.fail("should not be able to update a finalized game");
//    } catch (GameInProgressException e) {
//      // all good
//    }
//
//  }

}
