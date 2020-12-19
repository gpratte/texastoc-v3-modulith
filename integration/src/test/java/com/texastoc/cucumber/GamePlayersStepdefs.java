package com.texastoc.cucumber;

import com.texastoc.controller.request.CreateGamePlayerRequest;
import com.texastoc.controller.request.CreateGameRequest;
import com.texastoc.controller.request.UpdateGamePlayerRequest;
import com.texastoc.model.game.FirstTimeGamePlayer;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class GamePlayersStepdefs extends SpringBootBaseIntegrationTest {

  private Integer gameId;
  private Integer numPlayers;
  private Game gameRetrieved;
  private List<GamePlayer> gamePlayers = new LinkedList<>();
  private List<UpdateGamePlayerRequest> gamePlayersUpdated = new LinkedList<>();
  private List<FirstTimeGamePlayer> firstTimeGamePlayers = new LinkedList<>();

  private Random random = new Random(System.currentTimeMillis());

  @Before
  public void before() {
    gameId = null;
    numPlayers = null;
    gameRetrieved = null;
    gamePlayers.clear();
    gamePlayersUpdated.clear();
    firstTimeGamePlayers.clear();
  }


  @When("^a game is created$")
  public void a_game_is_created() throws Exception {
    // Arrange
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    createSeason(token);

    CreateGameRequest createGameRequest = CreateGameRequest.builder()
      .date(LocalDate.now())
      .hostId(1)
      .transportRequired(false)
      .build();

    gameId = createGame(createGameRequest, token).getId();
  }


  @And("^a player is added without buy-in$")
  public void a_player_is_added_without_buy_in() throws Exception {

    CreateGamePlayerRequest createGamePlayerRequest = CreateGamePlayerRequest.builder()
      .playerId(BRIAN_BAKER_PLAYER_ID)
      .gameId(gameId)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(createGamePlayerRequest, token));
  }

  @And("^a player is added with buy-in$")
  public void a_player_is_added_with_buy_in() throws Exception {

    CreateGamePlayerRequest createGamePlayerRequest = CreateGamePlayerRequest.builder()
      .playerId(BRIAN_BAKER_PLAYER_ID)
      .gameId(gameId)
      .buyInCollected(true)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(createGamePlayerRequest, token));
  }

  @And("^two players are added with buy-in$")
  public void two_players_are_added_with_buy_in() throws Exception {

    CreateGamePlayerRequest createGamePlayerRequest = CreateGamePlayerRequest.builder()
      .playerId(BRIAN_BAKER_PLAYER_ID)
      .gameId(gameId)
      .buyInCollected(true)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(createGamePlayerRequest, token));

    createGamePlayerRequest = CreateGamePlayerRequest.builder()
      .playerId(ANDY_THOMAS_PLAYER_ID)
      .gameId(gameId)
      .buyInCollected(true)
      .build();
    gamePlayers.add(addPlayerToGame(createGamePlayerRequest, token));
  }

  @And("^the game is retrieved$")
  public void the_game_is_retrieved() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameRetrieved = getGame(gameId, token);
  }

  @And("^the player is updated$")
  public void the_player_is_updated() throws Exception {

    GamePlayer gamePlayer = gamePlayers.get(0);

    UpdateGamePlayerRequest updateGamePlayerRequest = UpdateGamePlayerRequest.builder()
      .gamePlayerId(gamePlayer.getId())
      .gameId(gameId)
      .knockedOut(false)
      .roundUpdates(true)
      .buyInCollected(true)
      .rebuyAddOnCollected(true)
      .annualTocCollected(true)
      .quarterlyTocCollected(true)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);

    updatePlayerInGame(gamePlayer.getId(), updateGamePlayerRequest, token);
    gamePlayersUpdated.add(updateGamePlayerRequest);
  }

  @And("^the player is knocked out")
  public void knockedOut() throws Exception {

    GamePlayer gamePlayer = gamePlayers.get(0);

    UpdateGamePlayerRequest updateGamePlayerRequest = UpdateGamePlayerRequest.builder()
      .gamePlayerId(gamePlayer.getId())
      .gameId(gameId)
      .place(10)
      .knockedOut(true)
      .roundUpdates(true)
      .buyInCollected(true)
      .rebuyAddOnCollected(true)
      .annualTocCollected(true)
      .quarterlyTocCollected(true)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);

    updatePlayerInGame(gamePlayer.getId(), updateGamePlayerRequest, token);
    gamePlayersUpdated.add(updateGamePlayerRequest);
  }

  @And("^the player is deleted$")
  public void the_player_is_deleted() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    GamePlayer gamePlayer = gamePlayers.get(0);
    deletePlayerFromGame(gameId, gamePlayer.getId(), token);
  }

  @And("^a first time player is added$")
  public void a_first_time_player_is_added() throws Exception {

    FirstTimeGamePlayer firstTimeGamePlayer = FirstTimeGamePlayer.builder()
      .firstName("Joe")
      .lastName("Schmoe")
      .email("joe.schmoe@texastoc.com")
      .gameId(gameId)
      .buyInCollected(true)
      .annualTocCollected(true)
      .quarterlyTocCollected(true)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);

    firstTimeGamePlayers.add(firstTimeGamePlayer);
    gamePlayers.add(addFirstTimePlayerToGame(firstTimeGamePlayer, token));
  }

  @Then("^the retrieved game has one player no buy-in$")
  public void the_retrieved_game_has_one_player_no_buy_in() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertEquals("num of game payouts should be zero", 0, (int) gameRetrieved.getPayouts().size());
    Assert.assertNotNull("last calculated should be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 1", 1, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 1", 1, (int) gameRetrieved.getPlayers().size());

    GamePlayer expected = gamePlayers.get(0);
    GamePlayer actual = gameRetrieved.getPlayers().get(0);
    Assert.assertEquals("game player created game id should be " + expected.getGameId(), expected.getGameId(), actual.getGameId());
    Assert.assertEquals("game player created player id should be " + expected.getPlayerId(), expected.getPlayerId(), actual.getPlayerId());
    Assert.assertEquals("game player created name should be " + expected.getName(), expected.getName(), actual.getName());

    Assert.assertNull("the game player points should be null", actual.getPoints());
    Assert.assertNull("the game player buyInCollected should be null", actual.getBuyInCollected());
    Assert.assertNull("the game player rebuyAddOnCollected should be null", actual.getRebuyAddOnCollected());
    Assert.assertNull("the game player annualTocCollected should be null", actual.getAnnualTocCollected());
    Assert.assertNull("the game player quarterlyTocCollected should be null", actual.getQuarterlyTocCollected());
    Assert.assertNull("the game player chop should be null", actual.getChop());
    Assert.assertNull("the game player finish should be null", actual.getPlace());
    Assert.assertNull("the game player knockedOut should be null", actual.getKnockedOut());
    Assert.assertNull("the game player roundUpdates should be null", actual.getRoundUpdates());
  }

  @Then("^the retrieved game has one player with buy-in$")
  public void the_retrieved_game_has_one_player_with_buy_in() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertEquals("num of game payouts should be 0", 0, (int) gameRetrieved.getPayouts().size());
    Assert.assertEquals("kitty should be " + KITTY_PER_GAME, KITTY_PER_GAME, (int) gameRetrieved.getKittyCalculated());
    Assert.assertNotNull("last calculated should be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 1", 1, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 1", 1, (int) gameRetrieved.getPlayers().size());

    GamePlayer expected = gamePlayers.get(0);
    GamePlayer actual = gameRetrieved.getPlayers().get(0);

    Assert.assertNull("the game player points should be null", actual.getPoints());
    Assert.assertEquals("the game player buyInCollected should be " + GAME_BUY_IN, GAME_BUY_IN, (int) actual.getBuyInCollected());
    Assert.assertNull("the game player rebuyAddOnCollected should be null", actual.getRebuyAddOnCollected());
    Assert.assertNull("the game player annualTocCollected should be null", actual.getAnnualTocCollected());
    Assert.assertNull("the game player quarterlyTocCollected should be null", actual.getQuarterlyTocCollected());
    Assert.assertNull("the game player chop should be null", actual.getChop());
  }

  @Then("^the retrieved game has two players with buy-in$")
  public void the_retrieved_game_has_two_players_with_buy_in() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertEquals("num of game payouts should be 1", 1, (int) gameRetrieved.getPayouts().size());
    Assert.assertEquals("kitty should be " + KITTY_PER_GAME, KITTY_PER_GAME, (int) gameRetrieved.getKittyCalculated());
    Assert.assertNotNull("last calculated should be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 2", 2, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 2", 2, (int) gameRetrieved.getPlayers().size());

    for (int i = 0; i < gameRetrieved.getPlayers().size(); i++) {
      GamePlayer expected = gamePlayers.get(i);
      GamePlayer actual = gameRetrieved.getPlayers().get(i);

      Assert.assertNull("the game player points should be null", actual.getPoints());
      Assert.assertEquals("the game player buyInCollected should be " + GAME_BUY_IN, GAME_BUY_IN, (int) actual.getBuyInCollected());
      Assert.assertNull("the game player rebuyAddOnCollected should be null", actual.getRebuyAddOnCollected());
      Assert.assertNull("the game player annualTocCollected should be null", actual.getAnnualTocCollected());
      Assert.assertNull("the game player quarterlyTocCollected should be null", actual.getQuarterlyTocCollected());
      Assert.assertNull("the game player chop should be null", actual.getChop());
    }
  }

  @And("^random players are added$")
  public void random_players_are_added() throws Exception {

    numPlayers = 0;
    while (numPlayers < 3) {
      numPlayers = random.nextInt(50);
    }

    String token = login(USER_EMAIL, USER_PASSWORD);

    for (int i = 0; i < numPlayers; i++) {
      CreateGamePlayerRequest createGamePlayerRequest = CreateGamePlayerRequest.builder()
        .playerId(1)
        .gameId(gameId)
        .buyInCollected(true)
        .annualTocCollected(random.nextBoolean())
        .quarterlyTocCollected(random.nextBoolean())
        .build();

      gamePlayers.add(addPlayerToGame(createGamePlayerRequest, token));
    }
  }


  @Then("^the retrieved game has random players$")
  public void the_retrieved_game_has_random_players() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertTrue("num of game payouts should be greater than 0", gameRetrieved.getPayouts().size() > 0);
    Assert.assertEquals("kitty should be " + KITTY_PER_GAME, KITTY_PER_GAME, (int) gameRetrieved.getKittyCalculated());
    Assert.assertNotNull("last calculated should be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be " + gamePlayers.size(), gamePlayers.size(), gamePlayers.size(), (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be " + gamePlayers.size(), gamePlayers.size(), (int) gameRetrieved.getPlayers().size());

    for (int i = 0; i < gameRetrieved.getPlayers().size(); i++) {
      GamePlayer expected = gamePlayers.get(i);
      GamePlayer actual = gameRetrieved.getPlayers().get(i);

      Assert.assertNull("the game player points should be null", actual.getPoints());
      Assert.assertEquals("the game player buyInCollected should be " + GAME_BUY_IN, GAME_BUY_IN, (int) actual.getBuyInCollected());

      if (expected.getRebuyAddOnCollected() == null) {
        Assert.assertNull("the game player rebuyAddOnCollected should be null", actual.getRebuyAddOnCollected());
      } else {
        Assert.assertEquals("the game player rebuyAddOnCollected should be " + expected.getRebuyAddOnCollected(), expected.getRebuyAddOnCollected(), actual.getRebuyAddOnCollected());
      }

      if (expected.getAnnualTocCollected() == null) {
        Assert.assertNull("the game player annualTocCollected should be null", actual.getAnnualTocCollected());
      } else {
        Assert.assertEquals("the game player annualTocCollected should be " + expected.getAnnualTocCollected(), expected.getAnnualTocCollected(), actual.getAnnualTocCollected());
      }

      if (expected.getQuarterlyTocCollected() == null) {
        Assert.assertNull("the game player quarterlyTocCollected should be null", actual.getQuarterlyTocCollected());
      } else {
        Assert.assertEquals("the game player quarterlyTocCollected should be " + expected.getQuarterlyTocCollected(), expected.getQuarterlyTocCollected(), actual.getQuarterlyTocCollected());
      }

      Assert.assertNull("the game player chop should be null", actual.getChop());
    }
  }

  @Then("^the retrieved game has one player with updates$")
  public void the_retrieved_game_has_one_player_with_updates() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertEquals("num of game payouts should be 0", 0, (int) gameRetrieved.getPayouts().size());
    Assert.assertEquals("kitty should be " + KITTY_PER_GAME, KITTY_PER_GAME, (int) gameRetrieved.getKittyCalculated());
    Assert.assertNotNull("last calculated should not be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 1", 1, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 1", 1, (int) gameRetrieved.getPlayers().size());

    UpdateGamePlayerRequest expected = gamePlayersUpdated.get(0);
    GamePlayer actual = gameRetrieved.getPlayers().get(0);

    Assert.assertTrue("the game player points should be null or 0", actual.getPoints() == null || actual.getPoints() < 1);

    if (expected.getPlace() != null) {
      Assert.assertEquals("the game player finish should be " + expected.getPlace(), (int) expected.getPlace(), (int) actual.getPlace());
    } else {
      Assert.assertNull("the game player finish should not be set", actual.getPlace());
    }

    if (expected.isKnockedOut() || (expected.getPlace() != null && expected.getPlace() < 11)) {
      Assert.assertTrue("the game player should be knockedOut", actual.getKnockedOut());
    } else {
      Assert.assertTrue("the game player should not be knockedOut", actual.getKnockedOut() == null || !actual.getKnockedOut());
    }

    if (expected.isRoundUpdates()) {
      Assert.assertTrue("the game player should be in for round updates", actual.getRoundUpdates());
    } else {
      Assert.assertTrue("the game player should not be in for round updates", actual.getRoundUpdates() == null || !actual.getRoundUpdates());
    }

    if (expected.isBuyInCollected()) {
      Assert.assertEquals("the game player buyInCollected should be " + gameRetrieved.getBuyInCost(), gameRetrieved.getBuyInCost(), (int) actual.getBuyInCollected());
    } else {
      Assert.assertNull("the game player should not be bought in", actual.getBuyInCollected());
    }

    if (expected.isRebuyAddOnCollected()) {
      Assert.assertEquals("the game player rebuyAddOn should be " + gameRetrieved.getRebuyAddOnCost(), gameRetrieved.getRebuyAddOnCost(), (int) actual.getRebuyAddOnCollected());
    } else {
      Assert.assertNull("the game player should not have rebought", actual.getRebuyAddOnCollected());
    }

    if (expected.isAnnualTocCollected()) {
      Assert.assertEquals("the game player annual toc should be " + gameRetrieved.getAnnualTocCost(), gameRetrieved.getAnnualTocCost(), (int) actual.getAnnualTocCollected());
    } else {
      Assert.assertNull("the game player should not have annual toc", actual.getAnnualTocCollected());
    }

    if (expected.isQuarterlyTocCollected()) {
      Assert.assertEquals("the game player quarterly annual toc should be " + gameRetrieved.getQuarterlyTocCost(), gameRetrieved.getQuarterlyTocCost(), (int) actual.getQuarterlyTocCollected());
    } else {
      Assert.assertNull("the game player should not have quarterly annual toc", actual.getQuarterlyTocCollected());
    }

    Assert.assertNull("the game player chop should be null", actual.getChop());
  }

  @Then("^the retrieved game does not have the player$")
  public void the_retrieved_game_does_not_have_the_player() throws Exception {

    // Assert game
    Assert.assertNotNull("game payouts should not be null", gameRetrieved.getPayouts());
    Assert.assertEquals("num of game payouts should be 0", 0, (int) gameRetrieved.getPayouts().size());
    Assert.assertNotNull("last calculated should not be null", gameRetrieved.getLastCalculated());

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 0", 0, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 0", 0, (int) gameRetrieved.getPlayers().size());
  }

  @Then("^the retrieved game has the first time player$")
  public void the_retrieved_game_has_the_first_time_player() throws Exception {

    // Assert game player
    Assert.assertNotNull("game players should not be null", gameRetrieved.getPlayers());
    Assert.assertEquals("num of game players should be 1", 1, (int) gameRetrieved.getNumPlayers());
    Assert.assertEquals("num of game players in list should be 1", 1, (int) gameRetrieved.getPlayers().size());

    FirstTimeGamePlayer expected = firstTimeGamePlayers.get(0);
    GamePlayer actual = gameRetrieved.getPlayers().get(0);
    Assert.assertEquals("game player created game id should be " + expected.getGameId(), expected.getGameId(), actual.getGameId());
    Assert.assertTrue("game player created player id should be set", actual.getPlayerId() > 0);
    Assert.assertEquals("game player created name should be " + expected.getFirstName() + " " + expected.getLastName(), (expected.getFirstName() + " " + expected.getLastName()), actual.getName());

    Assert.assertNull("the game player points should be null", actual.getPoints());
    Assert.assertEquals("the game player buyInCollected should be " + GAME_BUY_IN, GAME_BUY_IN, (int) actual.getBuyInCollected());
    Assert.assertNull("the game player rebuyAddOnCollected should be null", actual.getRebuyAddOnCollected());
    Assert.assertEquals("the game player annual toc should be " + TOC_PER_GAME, TOC_PER_GAME, (int) actual.getAnnualTocCollected());
    Assert.assertEquals("the game player quarterly toc should be " + QUARTERLY_TOC_PER_GAME, QUARTERLY_TOC_PER_GAME, (int) actual.getQuarterlyTocCollected());
    Assert.assertNull("the game player chop should be null", actual.getChop());
    Assert.assertNull("the game player finish should be null", actual.getPlace());
    Assert.assertNull("the game player knockedOut should be null", actual.getKnockedOut());
    Assert.assertNull("the game player roundUpdates should be null", actual.getRoundUpdates());
  }

  @And("^paid players is (\\d+)$")
  public void paidPlayers(int numPaidPlayers) throws Exception {
    Assert.assertEquals("number of paid players should be " + numPaidPlayers, numPaidPlayers, gameRetrieved.getNumPaidPlayers());
  }

  @And("^paid players remaining is (\\d+)$")
  public void paidPlayersRemaining(int numPaidPlayersRemaining) throws Exception {
    Assert.assertEquals("number of paid players remaining should be " + numPaidPlayersRemaining, numPaidPlayersRemaining, gameRetrieved.getNumPaidPlayersRemaining());
  }

}
