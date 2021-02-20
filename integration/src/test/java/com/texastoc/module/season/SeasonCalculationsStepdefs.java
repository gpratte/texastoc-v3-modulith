package com.texastoc.module.season;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.season.model.Season;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Before;

public class SeasonCalculationsStepdefs extends BaseSeasonStepdefs {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Before
  public void before() {
    super.before();
  }

  @Given("^a season started encompassing today$")
  public void seasonExists() throws Exception {
    aSeasonExists();
  }

  @And("^a running game has players$")
  public void gameInProgress(String json) throws Exception {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, token);
    System.out.println("!!! game created " + gameCreated.getId());

    List<GamePlayer> gamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<GamePlayer>>() {
        });
    for (GamePlayer gp : gamePlayers) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .gameId(gameCreated.getId())
          .firstName(gp.getFirstName() == null ? "first" : gp.getFirstName())
          .lastName(gp.getLastName() == null ? "last" : gp.getLastName())
          .boughtIn(gp.isBoughtIn())
          .annualTocParticipant(gp.isAnnualTocParticipant())
          .quarterlyTocParticipant(gp.isQuarterlyTocParticipant())
          .rebought(gp.isRebought())
          .place(gp.getPlace())
          .chop(gp.getChop())
          .build();
      addFirstTimePlayerToGame(gamePlayer, token);
    }
  }

  @When("^the finalized game triggers the season to recalculate$")
  public void finalizeGame() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    System.out.println("!!! finalizing game " + gameCreated.getId());
    finalizeGame(gameCreated.getId(), token);
  }

  @Given("^the calculated season is retrieved with (\\d+) games played$")
  public void getCalcuatedSeason(int numGames) throws Exception {
    final String token = login(USER_EMAIL, USER_PASSWORD);
    Awaitility.await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          seasonRetrieved = super.getCurrentSeason(token);
          Assertions.assertThat(seasonRetrieved.getNumGamesPlayed()).isEqualTo(numGames);
        });
  }

  @Then("^the season calculations should be$")
  public void checkSeasonCalculations(String json) throws Exception {
    Season expectedSeason = OBJECT_MAPPER.readValue(json, Season.class);

    assertEquals(expectedSeason.getBuyInCollected(), seasonRetrieved.getBuyInCollected());
    assertEquals(expectedSeason.getRebuyAddOnCollected(), seasonRetrieved.getRebuyAddOnCollected());
    assertEquals(expectedSeason.getAnnualTocCollected(), seasonRetrieved.getAnnualTocCollected());
    assertEquals(expectedSeason.getTotalCollected(), seasonRetrieved.getTotalCollected());
    assertEquals(expectedSeason.getAnnualTocFromRebuyAddOnCalculated(),
        seasonRetrieved.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals(expectedSeason.getRebuyAddOnLessAnnualTocCalculated(),
        seasonRetrieved.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(expectedSeason.getTotalCombinedAnnualTocCalculated(),
        seasonRetrieved.getTotalCombinedAnnualTocCalculated());
    assertEquals(expectedSeason.getKittyCalculated(), seasonRetrieved.getKittyCalculated());
    assertEquals(expectedSeason.getPrizePotCalculated(), seasonRetrieved.getPrizePotCalculated());
    assertEquals(expectedSeason.getNumGames(), seasonRetrieved.getNumGames());
    assertEquals(expectedSeason.getNumGamesPlayed(), seasonRetrieved.getNumGamesPlayed());
    assertEquals(expectedSeason.isFinalized(), seasonRetrieved.isFinalized());
  }
}
