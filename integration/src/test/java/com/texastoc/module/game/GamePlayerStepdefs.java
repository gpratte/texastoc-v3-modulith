package com.texastoc.module.game;

import com.texastoc.module.game.model.FirstTimeGamePlayer;
import com.texastoc.module.game.model.GamePlayer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GamePlayerStepdefs extends BaseGameStepdefs {

  private Integer gameId;
  private Integer numPlayers;
  private List<GamePlayer> gamePlayers = new LinkedList<>();
  private List<GamePlayer> retrivedGamePlayers = new LinkedList<>();
  private List<GamePlayer> gamePlayersUpdated = new LinkedList<>();
  private List<FirstTimeGamePlayer> firstTimeGamePlayers = new LinkedList<>();

  private Random random = new Random(System.currentTimeMillis());

  @When("^before game player scenario$")
  public void before() {
    // Before each scenario
    super.before();
    gameId = null;
    numPlayers = null;
    gamePlayers.clear();
    gamePlayersUpdated.clear();
    firstTimeGamePlayers.clear();
  }

  @Then("^after game player scenario$")
  public void after() {
    // After each scenario
    super.after();
  }

  @When("^a game is created$")
  public void a_game_is_created() throws Exception {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  @And("^a player is added with nothing set$")
  public void aPlayerIsAddedWithNothingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
      .playerId(GUEST_USER_PLAYER_ID)
      .gameId(gameId)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a player is added with everything set$")
  public void aPlayerIsAddedWithEverythingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
      .playerId(GUEST_USER_PLAYER_ID)
      .gameId(gameId)
      .boughtIn(true)
      .rebought(true)
      .annualTocParticipant(true)
      .quarterlyTocParticipant(true)
      .knockedOut(true)
      .roundUpdates(true)
      .chop(111)
      .place(1)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a first time player is added with everything set$")
  public void aFirstTimePlayerIsAddedWithEverythingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
      .playerId(GUEST_USER_PLAYER_ID)
      .firstName("first")
      .lastName("last")
      .email("firstlast@example.com")
      .gameId(gameId)
      .boughtIn(true)
      .rebought(true)
      .annualTocParticipant(true)
      .quarterlyTocParticipant(true)
      .knockedOut(true)
      .roundUpdates(true)
      .chop(111)
      .place(1)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @And("^a first time player is added with nothing set$")
  public void aFirstTimePlayerIsAddedWithNothingSet() throws Exception {
    GamePlayer gamePlayer = GamePlayer.builder()
      .firstName("first")
      .lastName("last")
      .email("firstlast@example.com")
      .gameId(gameId)
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gamePlayers.add(addPlayerToGame(gamePlayer, token));
  }

  @When("^the game is updated with the players$")
  public void theGameIsUpdatedWithThePlayers() throws Exception {
    super.getCurrentGame();
    gameRetrieved.setPlayers(gamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  @When("^the game is updated with the updated players$")
  public void theGameIsUpdatedWithTheUpdatedPlayers() throws Exception {
    super.getCurrentGame();
    gameRetrieved.setPlayers(retrivedGamePlayers);
    String token = login(USER_EMAIL, USER_PASSWORD);
    updateGame(gameRetrieved.getId(), gameRetrieved, token);
  }

  @And("^the current players are retrieved$")
  public void theCurrentPlayersAreRetrieved() throws Exception {
    super.getCurrentGame();
    retrivedGamePlayers = gameRetrieved.getPlayers();
  }

  @Then("^the retrieved game players have nothing set$")
  public void theRetrievedGamePlayersHaveNothingSet() {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      assertFalse("bought-in should be false", gamePlayer.isBoughtIn());
      assertFalse("rebought should be false", gamePlayer.isRebought());
      assertFalse("annual toc participant should be false", gamePlayer.isAnnualTocParticipant());
      assertFalse("quarterly toc participant should be false", gamePlayer.isQuarterlyTocParticipant());
      assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      assertFalse("round updates should be false", gamePlayer.isRoundUpdates());
    }
  }

  @Then("^the retrieved first time game players have nothing set$")
  public void theRetrievedFirstTimeGamePlayersHaveNothingSet() {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      assertFalse("bought-in should be false", gamePlayer.isBoughtIn());
      assertFalse("rebought should be false", gamePlayer.isRebought());
      assertFalse("annual toc participant be false", gamePlayer.isAnnualTocParticipant());
      assertFalse("quarterly toc participant should be false", gamePlayer.isQuarterlyTocParticipant());
      assertFalse("knocked out should be false", gamePlayer.isKnockedOut());
      assertFalse("round updates should be false", gamePlayer.isRoundUpdates());
      assertEquals("first name should be first", "first", gamePlayer.getFirstName());
      assertEquals("last name should be last", "last", gamePlayer.getLastName());
      assertEquals("name should be first last", "first last", gamePlayer.getName());
      assertEquals("email should be firstlast@example.com", "firstlast@example.com", gamePlayer.getEmail());
    }
  }

  @Then("^the retrieved game players have everything set$")
  public void theRetrievedGamePlayersHaveEverythingSet() {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      assertTrue("bought-in should be true", gamePlayer.isBoughtIn());
      assertTrue("rebought should be true", gamePlayer.isRebought());
      assertTrue("annual toc participant be true", gamePlayer.isAnnualTocParticipant());
      assertTrue("quarterly toc participant should be true", gamePlayer.isQuarterlyTocParticipant());
      assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      assertTrue("round updates should be true", gamePlayer.isRoundUpdates());
      assertEquals("chop should be 111", 111, gamePlayer.getChop().intValue());
      assertEquals("place should be 1", 1, gamePlayer.getPlace().intValue());
    }
  }

  @Then("^the retrieved first time game players have everything set$")
  public void theRetrievedFirstTimeGamePlayersHaveEverythingSet() {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      assertTrue("bought-in should be true", gamePlayer.isBoughtIn());
      assertTrue("rebought should be true", gamePlayer.isRebought());
      assertTrue("annual toc participant be true", gamePlayer.isAnnualTocParticipant());
      assertTrue("quarterly toc participant should be true", gamePlayer.isQuarterlyTocParticipant());
      assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
      assertTrue("round updates should be true", gamePlayer.isRoundUpdates());
      assertEquals("chop should be 111", 111, gamePlayer.getChop().intValue());
      assertEquals("place should be 1", 1, gamePlayer.getPlace().intValue());
      assertEquals("last name should be last", "last", gamePlayer.getLastName());
      assertEquals("name should be first last", "first last", gamePlayer.getName());
      assertEquals("email should be firstlast@example.com", "firstlast@example.com", gamePlayer.getEmail());
    }
  }

  @Then("^the retrieved game players are knocked out$")
  public void theRetrievedGamePlayersAreKnockedOut() {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      assertTrue("knocked out should be true", gamePlayer.isKnockedOut());
    }
  }


  @And("^the game player is deleted$")
  public void the_player_is_deleted() throws Exception {
//    String token = login(USER_EMAIL, USER_PASSWORD);
//    GamePlayer gamePlayer = gamePlayers.get(0);
//    deletePlayerFromGame(gameId, gamePlayer.getId(), token);
  }

  @And("^the current players are updated$")
  public void theCurrentPlayersAreUpdated() throws Exception {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      gamePlayer.setKnockedOut(true);
      gamePlayer.setAnnualTocParticipant(true);
      gamePlayer.setBoughtIn(true);
      gamePlayer.setAnnualTocParticipant(true);
      gamePlayer.setChop(111);
      gamePlayer.setPlace(1);
      gamePlayer.setQuarterlyTocParticipant(true);
      gamePlayer.setRebought(true);
      gamePlayer.setRoundUpdates(true);
    }
  }

  @And("^the current players are knocked out")
  public void theCurrentPlayersAreKnockedOut() throws Exception {
    for (GamePlayer gamePlayer : retrivedGamePlayers) {
      gamePlayer.setKnockedOut(true);
    }
  }

}
