package com.texastoc.module.season;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.model.SeasonPlayer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Test;

public class SeasonCalculationsIT extends BaseSeasonIT {

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Before
  public void before() {
    super.before();
  }

  @Test
  public void calculateASeasonWithOneGame() throws Exception {
    seasonExists();
    gameInProgress("[" +
        "{" +
        "\"firstName\":\"abe\"," +
        "\"lastName\":\"abeson\"," +
        "\"boughtIn\":true," +
        "\"annualTocParticipant\":true," +
        "\"quarterlyTocParticipant\":true," +
        "\"rebought\":true," +
        "\"place\":1," +
        "\"chop\":null" +
        "}" +
        "]");
    finalizeGame();
    getCalcuatedSeason(1);
    checkSeasonCalculations("{" +
        "\"buyInCollected\":40," +
        "\"rebuyAddOnCollected\":40," +
        "\"annualTocCollected\":20," +
        "\"totalCollected\":120," +
        "\"annualTocFromRebuyAddOnCalculated\":20," +
        "\"rebuyAddOnLessAnnualTocCalculated\":20," +
        "\"totalCombinedAnnualTocCalculated\":40," +
        "\"kittyCalculated\":10," +
        "\"prizePotCalculated\":50," +
        "\"numGames\":52," +
        "\"numGamesPlayed\":1," +
        "\"finalized\":false," +
        "\"players\":[" +
        "{" +
        "\"name\":\"abe abeson\"," +
        "\"place\":1," +
        "\"points\":30," +
        "\"entries\":1" +
        "}" +
        "]," +
        "\"payouts\":[]," +
        "\"estimatedPayouts\":[]" +
        "}");
  }

//
//  Scenario: calculate a season with two games
//  Two games with enough players to generate estimated payouts
//  Given a season started encompassing today
//  And a running game has players
//    """
//[
//  {
//    "firstName":"abe",
//    "lastName":"abeson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":1,
//    "chop":null
//  },
//  {
//    "firstName":"bob",
//    "lastName":"bobson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":2,
//    "chop":null
//  },
//  {
//    "firstName":"coy",
//    "lastName":"coyson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":3,
//    "chop":null
//  }
//]
//    """
//  And the running game is finalized
//  And a running game has existing players
//    """
//[
//  {
//    "firstName":"abe",
//    "lastName":"abeson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":1,
//    "chop":null
//  },
//  {
//    "firstName":"bob",
//    "lastName":"bobson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":2,
//    "chop":null
//  },
//  {
//    "firstName":"coy",
//    "lastName":"coyson",
//    "boughtIn":true,
//    "annualTocParticipant":true,
//    "quarterlyTocParticipant":true,
//    "rebought":true,
//    "place":3,
//    "chop":null
//  }
//]
//    """
//  When the finalized game triggers the season to recalculate
//  Then the calculated season is retrieved with 2 games played
//  Then the season calculations should be
//    """
//{
//  "buyInCollected":240,
//  "rebuyAddOnCollected":240,
//  "annualTocCollected":120,
//  "totalCollected":720,
//  "annualTocFromRebuyAddOnCalculated":120,
//  "rebuyAddOnLessAnnualTocCalculated":120,
//  "totalCombinedAnnualTocCalculated":240,
//  "kittyCalculated":20,
//  "prizePotCalculated":340,
//  "numGames":52,
//  "numGamesPlayed":2,
//  "finalized":false,
//  "players":[
//    {
//      "name":"abe abeson",
//      "place":1,
//      "points":70,
//      "entries":2
//    },
//    {
//      "name":"bob bobson",
//      "place":2,
//      "points":54,
//      "entries":2
//    },
//    {
//      "name":"coy coyson",
//      "place":3,
//      "points":42,
//      "entries":2
//    }
//  ],
//  "payouts":[],
//  "estimatedPayouts":[
//    {
//      "place":1,
//      "amount":1649,
//      "guaranteed":true,
//      "estimated":true,
//      "cash":false
//    },
//    {
//      "place":2,
//      "amount":1598,
//      "guaranteed":false,
//      "estimated":true,
//      "cash":false
//    },
//    {
//      "place":3,
//      "amount":1348,
//      "guaranteed":false,
//      "estimated":true,
//      "cash":false
//    },
//    {
//      "place":4,
//      "amount":1273,
//      "guaranteed":false,
//      "estimated":true,
//      "cash":false
//    },
//    {
//      "place":5,
//      "amount":372,
//      "guaranteed":false,
//      "estimated":true,
//      "cash":true
//    }
//  ]
//}
//    """


  //@Given("^a season started encompassing today$")
  public void seasonExists() throws Exception {
    aSeasonExists();
  }

  //@And("^a running game has players$")
  public void gameInProgress(String json) throws Exception {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);

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

  @And("^a running game has existing players$")
  public void gameInProgressAddExistingPlayer(String json) throws Exception {
    // Create a game
    Game gameToCreate = Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, seasonCreated.getId(), token);

    List<SeasonPlayer> seasonPlayers = super.getSeason(seasonCreated.getId(), token).getPlayers();

    List<GamePlayer> gamePlayers = OBJECT_MAPPER.readValue(
        json, new TypeReference<List<GamePlayer>>() {
        });
    for (GamePlayer gp : gamePlayers) {
      SeasonPlayer seasonPlayer = seasonPlayers.stream()
          .filter(sp -> sp.getName().startsWith(gp.getFirstName()))
          .findFirst().get();
      GamePlayer gamePlayer = GamePlayer.builder()
          .gameId(gameCreated.getId())
          .playerId(seasonPlayer.getPlayerId())
          .boughtIn(gp.isBoughtIn())
          .annualTocParticipant(gp.isAnnualTocParticipant())
          .quarterlyTocParticipant(gp.isQuarterlyTocParticipant())
          .rebought(gp.isRebought())
          .place(gp.getPlace())
          .chop(gp.getChop())
          .build();
      addPlayerToGame(gamePlayer, token);
    }
  }

  @And("^the running game is finalized$")
  public void finalizedGame() throws JsonProcessingException {
    String token = login(USER_EMAIL, USER_PASSWORD);
    super.finalizeGame(gameCreated.getId(), token);
  }

  //@When("^the finalized game triggers the season to recalculate$")
  public void finalizeGame() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    finalizeGame(gameCreated.getId(), token);
  }

  //@Given("^the calculated season is retrieved with (\\d+) games played$")
  public void getCalcuatedSeason(int numGames) throws Exception {
    final String token = login(USER_EMAIL, USER_PASSWORD);
    Awaitility.await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          seasonRetrieved = super.getSeason(seasonCreated.getId(), token);
          Assertions.assertThat(seasonRetrieved.getNumGamesPlayed()).isEqualTo(numGames);
        });
  }

  //@Then("^the season calculations should be$")
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

    assertEquals(expectedSeason.getPlayers().size(), seasonRetrieved.getPlayers().size());
    for (int i = 0; i < expectedSeason.getPlayers().size(); i++) {
      assertEquals(seasonRetrieved.getId(), seasonRetrieved.getPlayers().get(i).getSeasonId());
      assertEquals(expectedSeason.getPlayers().get(i).getName(),
          seasonRetrieved.getPlayers().get(i).getName());
      if (expectedSeason.getPlayers().get(i).getPlace() == null) {
        assertNull(seasonRetrieved.getPlayers().get(i).getPlace());
      } else {
        assertEquals(expectedSeason.getPlayers().get(i).getPlace(),
            seasonRetrieved.getPlayers().get(i).getPlace());
      }
      assertEquals(expectedSeason.getPlayers().get(i).getPoints(),
          seasonRetrieved.getPlayers().get(i).getPoints());
      assertEquals(expectedSeason.getPlayers().get(i).getEntries(),
          seasonRetrieved.getPlayers().get(i).getEntries());
    }

    assertEquals(expectedSeason.getPayouts().size(), seasonRetrieved.getPayouts().size());
    for (int i = 0; i < expectedSeason.getPayouts().size(); i++) {
      assertEquals(seasonRetrieved.getId(), seasonRetrieved.getPayouts().get(i).getSeasonId());
      assertEquals(expectedSeason.getPayouts().get(i).getPlace(),
          seasonRetrieved.getPayouts().get(i).getPlace());
      assertEquals(expectedSeason.getPayouts().get(i).getAmount(),
          seasonRetrieved.getPayouts().get(i).getAmount());
      assertEquals(expectedSeason.getPayouts().get(i).isGuaranteed(),
          seasonRetrieved.getPayouts().get(i).isGuaranteed());
      assertEquals(expectedSeason.getPayouts().get(i).isEstimated(),
          seasonRetrieved.getPayouts().get(i).isEstimated());
      assertEquals(expectedSeason.getPayouts().get(i).isCash(),
          seasonRetrieved.getPayouts().get(i).isCash());
    }

    assertEquals(expectedSeason.getEstimatedPayouts().size(),
        seasonRetrieved.getEstimatedPayouts().size());
    for (int i = 0; i < expectedSeason.getEstimatedPayouts().size(); i++) {
      assertEquals(seasonRetrieved.getId(),
          seasonRetrieved.getEstimatedPayouts().get(i).getSeasonId());
      assertEquals(expectedSeason.getEstimatedPayouts().get(i).getPlace(),
          seasonRetrieved.getEstimatedPayouts().get(i).getPlace());
      assertEquals(expectedSeason.getEstimatedPayouts().get(i).getAmount(),
          seasonRetrieved.getEstimatedPayouts().get(i).getAmount());
      assertEquals(expectedSeason.getEstimatedPayouts().get(i).isGuaranteed(),
          seasonRetrieved.getEstimatedPayouts().get(i).isGuaranteed());
      assertEquals(expectedSeason.getEstimatedPayouts().get(i).isEstimated(),
          seasonRetrieved.getEstimatedPayouts().get(i).isEstimated());
      assertEquals(expectedSeason.getEstimatedPayouts().get(i).isCash(),
          seasonRetrieved.getEstimatedPayouts().get(i).isCash());
    }

  }
}
