package com.texastoc.module.game;

import com.texastoc.module.game.model.GamePlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class GameCalculationsStepdefs extends BaseGameStepdefs {

  private Integer gameId;

  @Before
  public void before() {
    // Before each scenario
    super.before();
    gameId = null;
  }

  @When("^a calculated game is created$")
  public void a_game_is_created() throws Exception {
    super.aSeasonExists();
    super.theGameStartsNow();
    super.theGameIsCreated();
    gameId = gameCreated.getId();
  }

  @When("^a player is added without buy-in$")
  public void addPlayerNoBuyin() throws Exception {
    super.getCurrentGame();
    GamePlayer gamePlayer = GamePlayer.builder()
      .playerId(GUEST_USER_PLAYER_ID)
      .gameId(gameId)
      .build();
    String token = login(USER_EMAIL, USER_PASSWORD);
    addPlayerToGame(gamePlayer, token);
  }

  @When("^the current calculated game is retrieved$")
  public void getCurrentGame() throws Exception {
    super.getCurrentGame();
  }

  @Then("^the game calculated is (.*)$")
  public void calcualatedGame(String json) throws Exception {
    System.out.println("!!! " + json);
  }


}
