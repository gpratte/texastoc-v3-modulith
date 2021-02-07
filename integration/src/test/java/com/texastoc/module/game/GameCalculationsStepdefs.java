package com.texastoc.module.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameCalculationsStepdefs extends BaseGameStepdefs {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
    Game game = OBJECT_MAPPER.readValue(json, Game.class);
    assertGame(game, gameRetrieved);
  }

  private void assertGame(Game expected, Game actual) throws Exception {
    assertEquals(expected.getBuyInCollected(), actual.getBuyInCollected());
    assertEquals(expected.getRebuyAddOnCollected(), actual.getRebuyAddOnCollected());
    assertEquals(expected.getAnnualTocCollected(), actual.getAnnualTocCollected());
    assertEquals(expected.getQuarterlyTocCollected(), actual.getQuarterlyTocCollected());
    assertEquals(expected.getTotalCollected(), actual.getTotalCollected());
    assertEquals(expected.getAnnualTocFromRebuyAddOnCalculated(), actual.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals(expected.getRebuyAddOnLessAnnualTocCalculated(), actual.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(expected.getTotalCombinedTocCalculated(), actual.getTotalCombinedTocCalculated());
    assertEquals(expected.getKittyCalculated(), actual.getKittyCalculated());
    assertEquals(expected.getPrizePotCalculated(), actual.getPrizePotCalculated());
    assertEquals(expected.getNumPlayers(), actual.getNumPlayers());
    assertEquals(expected.getNumPaidPlayers(), actual.getNumPaidPlayers());
    assertEquals(expected.isChopped(), actual.isChopped());
    assertEquals(expected.isFinalized(), actual.isFinalized());

    // TODO how is this field used?
    //assertEquals(expected.isCanRebuy(), actual.isCanRebuy());

    assertEquals(expected.getPayouts().size(), actual.getPayouts().size());
    List<GamePayout> expectedPayouts = expected.getPayouts();
    List<GamePayout> actualPayouts = actual.getPayouts();
    for (int i = 0; i < expectedPayouts.size(); i++) {
      GamePayout expectedPayout = expectedPayouts.get(i);
      GamePayout actualPayout = actualPayouts.get(i);
      assertEquals(expectedPayout.getPlace(), actualPayout.getPlace());
      assertEquals(expectedPayout.getAmount(), actualPayout.getAmount());
      if (expectedPayout.getChopAmount() == null) {
        Assert.assertNull(actualPayout.getChopAmount());
      } else {
        assertEquals(expectedPayout.getChopAmount().intValue(), actualPayout.getChopAmount().intValue());
      }
    }
  }


}
