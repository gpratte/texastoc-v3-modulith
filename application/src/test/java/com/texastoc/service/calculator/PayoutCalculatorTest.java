package com.texastoc.service.calculator;

import com.texastoc.TestConstants;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePayout;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.repository.GamePayoutRepository;
import com.texastoc.repository.PayoutRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
public class PayoutCalculatorTest implements TestConstants {

  private PayoutCalculator payoutCalculator;
  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private PayoutRepository payoutRepository;

  @MockBean
  private GamePayoutRepository gamePayoutRepository;

  @Before
  public void before() {
    payoutCalculator = new PayoutCalculator(payoutRepository, gamePayoutRepository);
  }

  @Test
  public void testNoPlayersNoPayouts() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(0)
      .prizePotCalculated(0)
      .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 0", 0, gamePayouts.size());
  }

  @Test
  public void test1PlayersNoPayouts() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(1)
      .prizePotCalculated(0)
      .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 0", 0, gamePayouts.size());
  }

  @Test
  public void test1Players1Payout() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(1)
      .prizePotCalculated(GAME_BUY_IN)
      .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 1", 1, gamePayouts.size());

    GamePayout gamePayout = gamePayouts.get(0);
    Assert.assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    Assert.assertEquals("payout amount should be " + GAME_BUY_IN, GAME_BUY_IN, gamePayout.getAmount());
    Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
    Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
  }

  @Test
  public void testUpTo7Players1Payout() {

    // Create between 1 and 7 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(8);
    }

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(GAME_BUY_IN * numPlayers)
      .build();

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 1", 1, gamePayouts.size());

    GamePayout gamePayout = gamePayouts.get(0);
    Assert.assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    Assert.assertEquals("payout amount should be " + (GAME_BUY_IN * numPlayers), GAME_BUY_IN * numPlayers, gamePayout.getAmount());
    Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
    Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
  }

  @Test
  public void test8To12Players2Payouts() {

    // Create between 8 and 12 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 7;

    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(2);
    int firstPlace = (int) Math.round(0.65 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.35 * prizePot);
    amounts.add(secondPlace);

    double leftover = prizePot - firstPlace - secondPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test13To17Players3Payouts() {

    // Create between 13 and 17 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 12;

    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(3)).thenReturn(TestConstants.getPayouts(3));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 3", 3, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(3);
    int firstPlace = (int) Math.round(0.50 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.30 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.20 * prizePot);
    amounts.add(thirdPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test18To22Players4Payouts() {

    // Create between 18 and 22 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 17;

    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(4)).thenReturn(TestConstants.getPayouts(4));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 4", 4, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(4);
    int firstPlace = (int) Math.round(0.45 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.25 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.18 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.12 * prizePot);
    amounts.add(fourthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace;
    leftover = Math.abs(leftover);
    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test23To27Players5Payouts() {

    // Create between 23 and 27 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 22;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(5)).thenReturn(TestConstants.getPayouts(5));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 5", 5, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(5);
    int firstPlace = (int) Math.round(0.40 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.23 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.16 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.12 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.09 * prizePot);
    amounts.add(fifthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test28To32Players6Payouts() {

    // Create between 28 and 32 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 27;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(6)).thenReturn(TestConstants.getPayouts(6));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 6", 6, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(6);
    int firstPlace = (int) Math.round(0.38 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.22 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.15 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.11 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.08 * prizePot);
    amounts.add(fifthPlace);
    int sixthPlace = (int) Math.round(0.06 * prizePot);
    amounts.add(sixthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace - sixthPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test33To37Players7Payouts() {

    // Create between 33 and 37 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 32;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(7)).thenReturn(TestConstants.getPayouts(7));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 7", 7, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(7);
    int firstPlace = (int) Math.round(0.35 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.21 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.15 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.11 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.08 * prizePot);
    amounts.add(fifthPlace);
    int sixthPlace = (int) Math.round(0.06 * prizePot);
    amounts.add(sixthPlace);
    int seventhPlace = (int) Math.round(0.04 * prizePot);
    amounts.add(seventhPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace - sixthPlace - seventhPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test38To42Players8Payouts() {

    // Create between 38 and 42 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 37;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(8)).thenReturn(TestConstants.getPayouts(8));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 8", 8, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(8);
    int firstPlace = (int) Math.round(0.335 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.20 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.145 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.11 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.08 * prizePot);
    amounts.add(fifthPlace);
    int sixthPlace = (int) Math.round(0.06 * prizePot);
    amounts.add(sixthPlace);
    int seventhPlace = (int) Math.round(0.04 * prizePot);
    amounts.add(seventhPlace);
    int eighthPlace = (int) Math.round(0.03 * prizePot);
    amounts.add(eighthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace - sixthPlace - seventhPlace - eighthPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test43To47Players9Payouts() {

    // Create between 43 and 47 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 42;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(9)).thenReturn(TestConstants.getPayouts(9));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 9", 9, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(9);
    int firstPlace = (int) Math.round(0.32 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.195 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.14 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.11 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.08 * prizePot);
    amounts.add(fifthPlace);
    int sixthPlace = (int) Math.round(0.06 * prizePot);
    amounts.add(sixthPlace);
    int seventhPlace = (int) Math.round(0.04 * prizePot);
    amounts.add(seventhPlace);
    int eighthPlace = (int) Math.round(0.03 * prizePot);
    amounts.add(eighthPlace);
    int ninthPlace = (int) Math.round(0.025 * prizePot);
    amounts.add(ninthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace - sixthPlace - seventhPlace - eighthPlace - ninthPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test48orMorePlayers10Payouts() {

    // Create 48 or more
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(50);
    }
    numPlayers += 47;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();
    Mockito.when(payoutRepository.get(10)).thenReturn(TestConstants.getPayouts(10));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 10", 10, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(10);
    int firstPlace = (int) Math.round(0.30 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.19 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.1325 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.105 * prizePot);
    amounts.add(fourthPlace);
    int fifthPlace = (int) Math.round(0.075 * prizePot);
    amounts.add(fifthPlace);
    int sixthPlace = (int) Math.round(0.055 * prizePot);
    amounts.add(sixthPlace);
    int seventhPlace = (int) Math.round(0.0375 * prizePot);
    amounts.add(seventhPlace);
    int eighthPlace = (int) Math.round(0.03 * prizePot);
    amounts.add(eighthPlace);
    int ninthPlace = (int) Math.round(0.0225 * prizePot);
    amounts.add(ninthPlace);
    int tenthPlace = (int) Math.round(0.015 * prizePot);
    amounts.add(tenthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace - fifthPlace - sixthPlace - seventhPlace - eighthPlace - ninthPlace - tenthPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }


  @Test
  public void test13To17Players4Payouts() {

    // Create between 13 and 17 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 12;

    int prizePot = GAME_BUY_IN * numPlayers;

    // Add a payout
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .payoutDelta(1)
      .build();

    Mockito.when(payoutRepository.get(4)).thenReturn(TestConstants.getPayouts(4));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 4", 4, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(4);
    int firstPlace = (int) Math.round(0.45 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.25 * prizePot);
    amounts.add(secondPlace);
    int thirdPlace = (int) Math.round(0.18 * prizePot);
    amounts.add(thirdPlace);
    int fourthPlace = (int) Math.round(0.12 * prizePot);
    amounts.add(fourthPlace);

    double leftover = prizePot - firstPlace - secondPlace - thirdPlace - fourthPlace;
    leftover = Math.abs(leftover);
    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void test13To17Players2Payouts() {

    // Create between 13 and 17 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(5);
    }
    numPlayers += 12;

    int prizePot = GAME_BUY_IN * numPlayers;

    // Remove a payout
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .payoutDelta(-1)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

    List<Integer> amounts = new ArrayList<>(2);
    int firstPlace = (int) Math.round(0.65 * prizePot);
    amounts.add(firstPlace);
    int secondPlace = (int) Math.round(0.35 * prizePot);
    amounts.add(secondPlace);

    double leftover = prizePot - firstPlace - secondPlace;
    leftover = Math.abs(leftover);

    int totalPaidOut = 0;

    for (int i = 0; i < gamePayouts.size(); ++i) {
      GamePayout gamePayout = gamePayouts.get(i);
      int amount = amounts.get(i);
      int place = i + 1;

      Assert.assertEquals("payout should be place " + place, place, gamePayout.getPlace());
      Assert.assertEquals(amount, gamePayout.getAmount(), leftover);
      Assert.assertNull("payout chop amount should be null", gamePayout.getChopAmount());
      Assert.assertNull("payout chop percentage should be null", gamePayout.getChopPercent());
      totalPaidOut += gamePayout.getAmount();
    }

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  /**
   * One player will enter the chop and update before the next player.
   */
  @Test
  public void test8Players1Chop() {

    int numPlayers = 8;

    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    List<GamePlayer> gamePlayers = new ArrayList<>(8);
    gamePlayers.add(GamePlayer.builder()
      .chop(100000)
      .place(1)
      .build());
    gamePlayers.add(GamePlayer.builder()
      .place(2)
      .build());

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

    int totalPaidOut = 0;

    GamePayout gamePayout = gamePayouts.get(0);
    Assert.assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    Assert.assertTrue("amount should be original amount", gamePayout.getAmount() > 0);
    Assert.assertNull("payout chop should be null", gamePayout.getChopAmount());

    gamePayout = gamePayouts.get(1);
    Assert.assertEquals("payout should be place 2", 2, gamePayout.getPlace());
    Assert.assertTrue("amount should be original amount", gamePayout.getAmount() > 0);
    Assert.assertNull("payout chop should be null", gamePayout.getChopAmount());
  }

  @Test
  public void test8Players2PayoutsChopped() {

    int numPlayers = 8;

    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    List<GamePlayer> gamePlayers = new ArrayList<>(8);
    gamePlayers.add(GamePlayer.builder()
      .chop(100000)
      .place(1)
      .build());
    gamePlayers.add(GamePlayer.builder()
      .chop(50000)
      .place(2)
      .build());

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

    int totalPaidOut = 0;

    GamePayout gamePayout = gamePayouts.get(0);
    Assert.assertEquals("payout should be place 1", 1, gamePayout.getPlace());
    Assert.assertTrue("amount should be original amount", gamePayout.getAmount() > 0);
    Assert.assertTrue("payout chop should be greater than 0", (int) gamePayout.getChopAmount() > 0);
    totalPaidOut += gamePayout.getChopAmount();

    gamePayout = gamePayouts.get(1);
    Assert.assertEquals("payout should be place 2", 2, gamePayout.getPlace());
    Assert.assertTrue("amount should be original amount", gamePayout.getAmount() > 0);
    Assert.assertTrue("payout chop should be greater than 0", gamePayout.getChopAmount() > 0);
    totalPaidOut += gamePayout.getChopAmount();

    Assert.assertEquals("sum of payouts for " + numPlayers + " players should be " + prizePot, prizePot, totalPaidOut);
  }

  @Test
  public void testNumPayoutsChanged() {

    int numPlayers = 10;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    // Currently no payouts
    Mockito.when(gamePayoutRepository.getByGameId(1)).thenReturn(Collections.EMPTY_LIST);

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
    Mockito.verify(gamePayoutRepository, Mockito.times(1)).deleteByGameId(1);
    // Two payouts persisted
    Mockito.verify(gamePayoutRepository, Mockito.times(2)).save(Mockito.any(GamePayout.class));

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

  }

  @Test
  public void testPayoutsChanged() {

    int numPlayers = 10;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    // Set the current payouts such that at least one will be different from the new payouts
    List<GamePayout> currentGamePayouts = new ArrayList<>(2);
    currentGamePayouts.add(GamePayout.builder()
      .gameId(1)
      .place(1)
      .amount(1000)
      .build());
    currentGamePayouts.add(GamePayout.builder()
      .gameId(1)
      .place(2)
      .amount(999)
      .build());

    // Currently no payouts
    Mockito.when(gamePayoutRepository.getByGameId(1)).thenReturn(currentGamePayouts);

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
    Mockito.verify(gamePayoutRepository, Mockito.times(1)).deleteByGameId(1);
    // Two payouts persisted
    Mockito.verify(gamePayoutRepository, Mockito.times(2)).save(Mockito.any(GamePayout.class));

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

  }

  @Test
  public void testPayoutsUnchanged() {

    int numPlayers = 10;
    int prizePot = GAME_BUY_IN * numPlayers;
    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .prizePotCalculated(prizePot)
      .build();

    Mockito.when(payoutRepository.get(2)).thenReturn(TestConstants.getPayouts(2));

    // Set the current payouts to be the same as the new payouts
    List<GamePayout> currentGamePayouts = new ArrayList<>(2);
    currentGamePayouts.add(GamePayout.builder()
      .gameId(1)
      .place(1)
      .amount(39)
      .build());
    currentGamePayouts.add(GamePayout.builder()
      .gameId(1)
      .place(2)
      .amount(21)
      .build());

    // Currently no payouts
    Mockito.when(gamePayoutRepository.getByGameId(1)).thenReturn(currentGamePayouts);

    List<GamePayout> gamePayouts = payoutCalculator.calculate(game, Collections.EMPTY_LIST);

    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
    Mockito.verify(gamePayoutRepository, Mockito.times(0)).deleteByGameId(1);
    // Two payouts persisted
    Mockito.verify(gamePayoutRepository, Mockito.times(0)).save(Mockito.any(GamePayout.class));

    Assert.assertNotNull("list of game payouts should not be null", gamePayouts);
    Assert.assertEquals("list of game payouts should be size 2", 2, gamePayouts.size());

  }


}
