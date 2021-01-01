package com.texastoc.module.game.service;

import com.texastoc.TestConstants;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class GamePlayerServiceTest implements TestConstants {

  private GamePlayerService gamePlayerService;

  private GameRepository gameRepository;
  private GameHelper gameHelper;

  private PlayerModule playerModule;

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameHelper = mock(GameHelper.class);
    playerModule = mock(PlayerModule.class);
    gamePlayerService = new GamePlayerService(gameRepository, gameHelper);
    ReflectionTestUtils.setField(gamePlayerService, "playerModule", playerModule);
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
}
