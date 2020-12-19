package com.texastoc.service.calculator;

import com.texastoc.TestConstants;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.repository.ConfigRepository;
import com.texastoc.repository.GameRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.notNull;

@RunWith(SpringRunner.class)
public class GameCalculatorTest implements TestConstants {

  private GameCalculator gameCalculator;

  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private GameRepository gameRepository;
  @MockBean
  private ConfigRepository configRepository;

  @Before
  public void before() {
    gameCalculator = new GameCalculator(gameRepository, configRepository);
  }

  @Test
  public void testNoGamePlayers() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(Integer.MAX_VALUE)

      .buyInCollected(Integer.MAX_VALUE)
      .rebuyAddOnCollected(Integer.MAX_VALUE)
      .annualTocCollected(Integer.MAX_VALUE)
      .quarterlyTocCollected(Integer.MAX_VALUE)
      .totalCollected(Integer.MAX_VALUE)

      .kittyCalculated(Integer.MAX_VALUE)
      .annualTocFromRebuyAddOnCalculated(Integer.MAX_VALUE)
      .rebuyAddOnLessAnnualTocCalculated(Integer.MAX_VALUE)
      .totalCombinedTocCalculated(Integer.MAX_VALUE)
      .prizePotCalculated(Integer.MAX_VALUE)

      .finalized(false)
      .lastCalculated(LocalDateTime.now().minusHours(1))
      .build();

    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());

    Mockito.doNothing().when(gameRepository).update((Game) notNull());

    LocalDateTime started = LocalDateTime.now();
    Game gameCalculated = gameCalculator.calculate(game, Collections.emptyList());

    Mockito.verify(configRepository, Mockito.times(0)).get();
    Mockito.verify(gameRepository, Mockito.times(1)).update(Mockito.any(Game.class));

    Assert.assertNotNull("game calculated should not be null", gameCalculated);

    Assert.assertEquals("number of game players should be 0", 0, (int) gameCalculated.getNumPlayers());

    Assert.assertEquals("buy-in collected should be 0", 0, (int) gameCalculated.getBuyInCollected());
    Assert.assertEquals("rebuy add on collected should be 0", 0, (int) gameCalculated.getRebuyAddOnCollected());
    Assert.assertEquals("annual toc collected should be 0", 0, (int) gameCalculated.getAnnualTocCollected());
    Assert.assertEquals("quarterly toc collected should be 0", 0, (int) gameCalculated.getQuarterlyTocCollected());
    Assert.assertEquals("total collected should be 0", 0, (int) gameCalculated.getTotalCollected());

    Assert.assertEquals("kitty calculated should be 0", 0, (int) gameCalculated.getKittyCalculated());
    Assert.assertEquals("rebuy add on toc calculated should be 0", 0, (int) gameCalculated.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuy add on less toc calculated should be 0", 0, (int) gameCalculated.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("total toc calculated should be 0", 0, (int) gameCalculated.getTotalCombinedTocCalculated());
    Assert.assertEquals("prize pot should be 0", 0, (int) gameCalculated.getPrizePotCalculated());

    Assert.assertFalse("not finalized", gameCalculated.isFinalized());

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastCalculated = gameCalculated.getLastCalculated();
    boolean isBetweenStartAndNow = (started.isBefore(lastCalculated) || started.isEqual(lastCalculated)) && (now.isAfter(lastCalculated) || now.isEqual(lastCalculated));
    Assert.assertTrue("last calculated should be between start and now", isBetweenStartAndNow);
  }

  @Test
  public void testGamePlayersNoBuyIns() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(Integer.MAX_VALUE)

      .buyInCollected(Integer.MAX_VALUE)
      .rebuyAddOnCollected(Integer.MAX_VALUE)
      .annualTocCollected(Integer.MAX_VALUE)
      .quarterlyTocCollected(Integer.MAX_VALUE)
      .totalCollected(Integer.MAX_VALUE)

      .kittyCalculated(Integer.MAX_VALUE)
      .annualTocFromRebuyAddOnCalculated(Integer.MAX_VALUE)
      .rebuyAddOnLessAnnualTocCalculated(Integer.MAX_VALUE)
      .totalCombinedTocCalculated(Integer.MAX_VALUE)
      .prizePotCalculated(Integer.MAX_VALUE)

      .finalized(false)
      .lastCalculated(LocalDateTime.now().minusHours(1))
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>();
    int playersToCreate = random.nextInt(10);
    for (int i = 0; i < playersToCreate; ++i) {
      GamePlayer gamePlayer = GamePlayer.builder()
        .id(i)
        .playerId(i)
        .gameId(1)
        .build();
      gamePlayers.add(gamePlayer);
    }

    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());

    Mockito.doNothing().when(gameRepository).update((Game) notNull());

    Game gameCalculated = gameCalculator.calculate(game, gamePlayers);

    Mockito.verify(configRepository, Mockito.times(0)).get();
    Mockito.verify(gameRepository, Mockito.times(1)).update(Mockito.any(Game.class));

    Assert.assertNotNull("game calculated should not be null", gameCalculated);

    Assert.assertEquals("number of game players should be " + playersToCreate, playersToCreate, (int) gameCalculated.getNumPlayers());

    Assert.assertEquals("buy-in collected should be 0", 0, (int) gameCalculated.getBuyInCollected());
    Assert.assertEquals("rebuy add on collected should be 0", 0, (int) gameCalculated.getRebuyAddOnCollected());
    Assert.assertEquals("annual toc collected should be 0", 0, (int) gameCalculated.getAnnualTocCollected());
    Assert.assertEquals("quarterly toc collected should be 0", 0, (int) gameCalculated.getQuarterlyTocCollected());
    Assert.assertEquals("total collected should be 0", 0, (int) gameCalculated.getTotalCollected());

    Assert.assertEquals("kitty calculated should be 0", 0, (int) gameCalculated.getKittyCalculated());
    Assert.assertEquals("rebuy add on toc calculated should be 0", 0, (int) gameCalculated.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuy add on less toc calculated should be 0", 0, (int) gameCalculated.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("total toc calculated should be 0", 0, (int) gameCalculated.getTotalCombinedTocCalculated());
    Assert.assertEquals("prize pot should be 0", 0, (int) gameCalculated.getPrizePotCalculated());

    Assert.assertFalse("not finalized", gameCalculated.isFinalized());
  }

  /**
   * One of each means
   * <table>
   *     <tr>
   *         <th>Player Id</th>
   *         <th>Buy-in</th>
   *         <th>Annual TOC</th>
   *         <th>Quarterly TOC</th>
   *         <th>Rebuy</th>
   *     </tr>
   *     <tr>
   *         <td>1</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>2</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>3</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *     </tr>
   *     <tr>
   *         <td>4</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *     </tr>
   *
   *     <tr>
   *         <td>5</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>6</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>7</td>
   *         <td>YES</td>
   *         <td>NO</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *     </tr>
   *     <tr>
   *         <td>8</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *         <td>YES</td>
   *     </tr>
   * </table>
   * The results for game should be (given the TestConstants)
   * <ul>
   *     <li>Number of players = 8</li>
   *     <li>Kitty collected = 9</li>
   *     <li>Buy-in collected = 48</li>
   *     <li>Rebuy Add On collected = 20</li>
   *     <li>Annual TOC collected = 32</li>
   *     <li>Quarterly TOC collected = 28</li>
   *     <li>Rebuy Add On TOC collected = 8</li>
   *     <li>Total collected = 48 (buy-in) + 20 (rebuy) + 32 (toc) + 28 (qtoc) = 128</li>
   *     <li>Total TOC collected = 32 (toc) + 28 (qtoc) + 8 (rebuy toc) = 68</li>
   *     <li>prizePot = total collected minus total toc minus kitty =  51</li>
   * </ul>
   */
  @Test
  public void testGamePlayerOneOfEach() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(Integer.MAX_VALUE)

      .buyInCollected(Integer.MAX_VALUE)
      .rebuyAddOnCollected(Integer.MAX_VALUE)
      .annualTocCollected(Integer.MAX_VALUE)
      .quarterlyTocCollected(Integer.MAX_VALUE)
      .totalCollected(Integer.MAX_VALUE)

      .kittyCalculated(Integer.MAX_VALUE)
      .annualTocFromRebuyAddOnCalculated(Integer.MAX_VALUE)
      .rebuyAddOnLessAnnualTocCalculated(Integer.MAX_VALUE)
      .totalCombinedTocCalculated(Integer.MAX_VALUE)
      .prizePotCalculated(Integer.MAX_VALUE)

      .finalized(false)
      .lastCalculated(LocalDateTime.now())
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>();
    GamePlayer gamePlayer = GamePlayer.builder()
      .id(1)
      .playerId(1)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(2)
      .playerId(2)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .annualTocCollected(TOC_PER_GAME)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(3)
      .playerId(3)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .quarterlyTocCollected(QUARTERLY_TOC_PER_GAME)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(4)
      .playerId(4)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .annualTocCollected(TOC_PER_GAME)
      .quarterlyTocCollected(QUARTERLY_TOC_PER_GAME)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(5)
      .playerId(5)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .rebuyAddOnCollected(TestConstants.GAME_REBUY)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(6)
      .playerId(6)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .annualTocCollected(TOC_PER_GAME)
      .rebuyAddOnCollected(TestConstants.GAME_REBUY)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(7)
      .playerId(7)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .quarterlyTocCollected(QUARTERLY_TOC_PER_GAME)
      .rebuyAddOnCollected(TestConstants.GAME_REBUY)
      .build();
    gamePlayers.add(gamePlayer);

    gamePlayer = GamePlayer.builder()
      .id(8)
      .playerId(8)
      .gameId(1)
      .buyInCollected(GAME_BUY_IN)
      .annualTocCollected(TOC_PER_GAME)
      .quarterlyTocCollected(QUARTERLY_TOC_PER_GAME)
      .rebuyAddOnCollected(TestConstants.GAME_REBUY)
      .build();
    gamePlayers.add(gamePlayer);


    Mockito.when(configRepository.get()).thenReturn(TestConstants.getTocConfig());

    Mockito.doNothing().when(gameRepository).update((Game) notNull());

    Game gameCalculated = gameCalculator.calculate(game, gamePlayers);

    Mockito.verify(configRepository, Mockito.times(1)).get();
    Mockito.verify(gameRepository, Mockito.times(1)).update(Mockito.any(Game.class));

    Assert.assertNotNull("game calculated should not be null", gameCalculated);

    Assert.assertEquals("number of game players should be 8", 8, (int) gameCalculated.getNumPlayers());

    Assert.assertEquals("buy-in collected should be 48", 48, (int) gameCalculated.getBuyInCollected());
    Assert.assertEquals("rebuy add on collected should be 20", 20, (int) gameCalculated.getRebuyAddOnCollected());
    Assert.assertEquals("annual toc collected should be 32", 32, (int) gameCalculated.getAnnualTocCollected());
    Assert.assertEquals("quarterly toc collected should be 28", 28, (int) gameCalculated.getQuarterlyTocCollected());
    Assert.assertEquals("total collected should be 128", 128, (int) gameCalculated.getTotalCollected());

    Assert.assertEquals("kitty calculated should be 9", 9, (int) gameCalculated.getKittyCalculated());
    Assert.assertEquals("annual Toc from rebuy add on calculated should be 8", 8, (int) gameCalculated.getAnnualTocFromRebuyAddOnCalculated());
    Assert.assertEquals("rebuy add on less annual Toc calculated should be 12", 12, (int) gameCalculated.getRebuyAddOnLessAnnualTocCalculated());
    Assert.assertEquals("total combined toc calculated should be 68", 68, (int) gameCalculated.getTotalCombinedTocCalculated());
    Assert.assertEquals("prize pot calculated should be 51", 51, (int) gameCalculated.getPrizePotCalculated());

  }
}
