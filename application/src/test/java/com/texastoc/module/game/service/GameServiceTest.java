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
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GameServiceTest implements TestConstants {

  private static final Random RANDOM = new Random(System.currentTimeMillis());

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

//  /**
//   * Somewhat of an anorexic test since there are no players but then again
//   * the game service code is just a pass through to the repositories.
//   */
//  @Test
//  public void getGameNoPlayers() {
//
//    Mockito.when(gameRepository.getById(1))
//      .thenReturn(Game.builder()
//        .id(1)
//        .build());
//
//    Mockito.when(gamePlayerRepository.selectByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Mockito.when(gamePayoutRepository.getByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Game game = gameService.get(1);
//
//    // Game repository called once
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//    assertNotNull("Game returned should not be null", game);
//    assertEquals("Game id should be 1", 1, (int) game.getId());
//
//    // GamePlayer repository called once
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);
//    assertNotNull("GamePlayers returned should not be null", game.getPlayers());
//    assertEquals("number of players should be 0", 0, game.getPlayers().size());
//
//    // GamePayout repository called once
//    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
//    assertNotNull("GamePayouts returned should not be null", game.getPayouts());
//    assertEquals("number of payouts should be 0", 0, game.getPayouts().size());
//  }
//
//  /**
//   * The current game is the (only) game that is not finalized
//   */
//  @Ignore
//  @Test
//  public void getCurrentGame() {
//    Season season = Season.builder()
//      .id(1)
//      .build();
//    Mockito.when(seasonRepository.getCurrent())
//      .thenReturn(season);
//
//    List<Game> games = new LinkedList<>();
//    games.add(Game.builder()
//      .id(1)
//      .build());
//    Mockito.when(gameRepository.getMostRecent(1))
//      .thenReturn(games);
//
//    Mockito.when(gamePlayerRepository.selectByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Mockito.when(gamePayoutRepository.getByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Game game = gameService.getCurrentGame();
//
//    // Season repository called once
//    Mockito.verify(seasonRepository, Mockito.times(1)).getCurrent();
//
//    // Game repository called once
//    Mockito.verify(gameRepository, Mockito.times(1)).getMostRecent(1);
//    assertNotNull("Game returned should not be null", game);
//    assertEquals("Game id should be 1", 1, (int) game.getId());
//
//    // GamePlayer repository called once
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);
//    assertNotNull("GamePlayers returned should not be null", game.getPlayers());
//    assertEquals("number of players should be 0", 0, game.getPlayers().size());
//
//    // GamePayout repository called once
//    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
//    assertNotNull("GamePayouts returned should not be null", game.getPayouts());
//    assertEquals("number of payouts should be 0", 0, game.getPayouts().size());
//  }
//
//  /**
//   * There is not current (no games that are not finalized) game
//   */
//  @Ignore
//  @Test
//  public void noCurrentGame() {
//
//    Season season = Season.builder()
//      .id(1)
//      .build();
//    Mockito.when(seasonRepository.getCurrent())
//      .thenReturn(season);
//
//    Mockito.when(gameRepository.getMostRecent(1))
//      .thenReturn(Collections.emptyList());
//
//    Game game = gameService.getCurrentGame();
//    assertNull("Current game returned should be null", game);
//  }
//
//  @Test
//  public void testUpdateGame() {
//
//    Mockito.when(gameRepository.getById(1)).thenReturn(Game.builder()
//      .id(1)
//      .finalized(false)
//      .build());
//
//    Mockito.doNothing().when(gameRepository).update((Game) notNull());
//
//    Game game = Game.builder()
//      .id(1)
//      .build();
//    gameService.updateGame(game);
//
//    Mockito.verify(gameRepository, Mockito.times(1)).update(any(Game.class));
//  }
//
//  @Ignore
//  @Test
//  public void testCreateGamePlayer() {
//
//    // GameService#createGamePlayers calls
//    // 1. gameRepository.getById
//    // 2. playerRepository.get(id)
//    // 3. gamePlayerRepository.save
//    // Then calls recalculate on a separate thread
//    // Not verifying the calculators because they have their own tests
//
//    Mockito.when(gamePlayerRepository.save((GamePlayer) notNull())).thenReturn(1);
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(0)
//      .finalized(false)
//      .build();
//    // 1. gameRepository.getById
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
////    Player player = Player.builder()
////      .id(1)
////      .firstName("bob")
////      .lastName("cob")
////      .build();
////    // 2. playerRepository.get(id)
////    Mockito.when(playerRepository.get(1)).thenReturn(player);
//
//    String playerName = Long.toString(System.currentTimeMillis());
//    GamePlayer gamePlayerToCreated = GamePlayer.builder()
//      .gameId(1)
//      .playerId(1)
//      .build();
//
//    // 3. gamePlayerRepository.save
//    Mockito.when(gamePlayerRepository.save(gamePlayerToCreated)).thenReturn(1);
//
//    CreateGamePlayerRequest cgpr = CreateGamePlayerRequest.builder()
//      .playerId(1)
//      .build();
//    GamePlayer gamePlayerCreated = gameService.createGamePlayer(1, cgpr);
//
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
////    Mockito.verify(playerRepository, Mockito.times(1)).get(1);
//
//    ArgumentCaptor<GamePlayer> gamePlayerArg = ArgumentCaptor.forClass(GamePlayer.class);
//    Mockito.verify(gamePlayerRepository).save(gamePlayerArg.capture());
//    assertEquals(1, gamePlayerArg.getValue().getGameId());
//    assertEquals(1, gamePlayerArg.getValue().getPlayerId());
//    assertEquals("bob cob", gamePlayerArg.getValue().getName());
//
//    assertNotNull("game player created should not be null", gamePlayerCreated);
//    assertEquals("game player id should be 1", 1, gamePlayerCreated.getGameId());
//    assertEquals("game player id should be 1", 1, gamePlayerCreated.getPlayerId());
//    assertEquals("game player name should be " + "bob cob", "bob cob", gamePlayerCreated.getName());
//
//    assertNull("game player points should be null", gamePlayerCreated.getPoints());
//
//    assertNull("game player finish should be null", gamePlayerCreated.getPlace());
//    assertNull("game player knocked out should be null", gamePlayerCreated.getKnockedOut());
//    assertNull("game player round updates should be null", gamePlayerCreated.getRoundUpdates());
//    assertNull("game player buy-in collected should be null", gamePlayerCreated.getBuyInCollected());
//    assertNull("game player rebuy add on collected should be null", gamePlayerCreated.getRebuyAddOnCollected());
//    assertNull("game player annual toc collected should be null", gamePlayerCreated.getAnnualTocCollected());
//    assertNull("game player quarterly toc collected should be null", gamePlayerCreated.getQuarterlyTocCollected());
//    assertNull("game player chop should be null", gamePlayerCreated.getChop());
//  }
//
//  @Test
//  public void testUpdateGamePlayer() {
//    // GameService#updateGamePlayer calls
//    // 1. gameRepository.getById
//    // 2. gamePlayerRepository.selectById(id)
//    // 3. gamePlayerRepository.update
//    // Then calls recalculate on a separate thread
//    // Not verifying the calculators because they have their own tests
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(1)
//      .buyInCost(GAME_BUY_IN)
//      .annualTocCost(TOC_PER_GAME)
//      .quarterlyTocCost(QUARTERLY_TOC_PER_GAME)
//      .rebuyAddOnCost(GAME_REBUY)
//      .finalized(false)
//      .build();
//    // 1. gameRepository.getById
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
//    GamePlayer gamePlayer = GamePlayer.builder()
//      .id(1)
//      .gameId(1)
//      .build();
//    // 2. gamePlayerRepository.selectById(id)
//    Mockito.when(gamePlayerRepository.selectById(1)).thenReturn(gamePlayer);
//
//    // Same as game player
//    UpdateGamePlayerRequest ugpr = UpdateGamePlayerRequest.builder()
//      .buyInCollected(true)
//      .rebuyAddOnCollected(true)
//      .annualTocCollected(true)
//      .quarterlyTocCollected(true)
//      .roundUpdates(true)
//      .place(10)
//      .knockedOut(true)
//      .chop(500)
//      .build();
//
//    GamePlayer gamePlayerUpdated = gameService.updateGamePlayer(1, 1, ugpr);
//
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).update(any(GamePlayer.class));
//
//    assertNotNull("game player updated should not be null", gamePlayerUpdated);
//    assertEquals("game player id should be 1", 1, gamePlayerUpdated.getGameId());
//    assertEquals("game player id should be 1", 1, gamePlayerUpdated.getId());
//
//    assertNull("game player points should be null", gamePlayerUpdated.getPoints());
//
//    assertEquals("game player finish should be 10", 10, gamePlayerUpdated.getPlace().intValue());
//    assertTrue("game player knocked out should be true", gamePlayerUpdated.getKnockedOut());
//    assertTrue("game player round updates should be true", gamePlayerUpdated.getRoundUpdates());
//    assertEquals("game player buy-in collected should be " + GAME_BUY_IN, GAME_BUY_IN, gamePlayerUpdated.getBuyInCollected().intValue());
//    assertEquals("game player rebuy add on collected should be " + GAME_REBUY, GAME_REBUY, gamePlayerUpdated.getRebuyAddOnCollected().intValue());
//    assertEquals("game player annual toc collected should be " + TOC_PER_GAME, TOC_PER_GAME, gamePlayerUpdated.getAnnualTocCollected().intValue());
//    assertEquals("game player quarterly toc collected should be " + QUARTERLY_TOC_PER_GAME, QUARTERLY_TOC_PER_GAME, gamePlayerUpdated.getQuarterlyTocCollected().intValue());
//    assertEquals("game player chop should be 500", 500, gamePlayerUpdated.getChop().intValue());
//  }
//
//  @Test
//  public void testDeleteGamePlayer() {
//
//    GamePlayer gamePlayer = GamePlayer.builder()
//      .id(1)
//      .gameId(1)
//      .build();
//    Mockito.when(gamePlayerRepository.selectById(1)).thenReturn(gamePlayer);
//
//    Mockito.doNothing().when(gamePlayerRepository).deleteById(1, 1);
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(0)
//      .build();
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
//    gameService.deleteGamePlayer(1, 1);
//
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).deleteById(1, 1);
//    Mockito.verify(gameRepository, Mockito.times(2)).getById(1);
//  }
//
//  @Ignore
//  @Test
//  public void testFirstTimeGamePlayer() {
//
//    // GameService#createFirstTimeGamePlayer calls
//    // 1. gameRepository.getById
//    // 2. playerRepository.save
//    // 3. roleRepository.save
//    // 4. gamePlayerRepository.save
//    // Then calls recalculate on a separate thread
//    // Not verifying the calculators because they have their own tests
//
//    Mockito.when(gamePlayerRepository.save((GamePlayer) notNull())).thenReturn(1);
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(0)
//      .finalized(false)
//      .build();
//    // 1. gameRepository.getById
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
//    // 2. playerRepository.save
////    Mockito.when(playerRepository.save((Player) notNull())).thenReturn(1);
//
//    // Not mocking 3. because it does not return anything and it is verified below
//
//    // 4. gamePlayerRepository.save
//    Mockito.when(gamePlayerRepository.save(any(GamePlayer.class))).thenReturn(1);
//
//    FirstTimeGamePlayer firstTimeGamePlayer = FirstTimeGamePlayer.builder()
//      .firstName("John")
//      .lastName("Doe")
//      .email("johndoe@texastoc.com")
//      .build();
//    GamePlayer actualGamePlayer = gameService.createFirstTimeGamePlayer(1, firstTimeGamePlayer);
//
//    assertNotNull(actualGamePlayer);
//    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).save(any(GamePlayer.class));
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//  }
//
//  @Ignore
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
