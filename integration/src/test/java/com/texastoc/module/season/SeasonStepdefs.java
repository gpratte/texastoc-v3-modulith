package com.texastoc.module.season;

import static org.junit.Assert.assertEquals;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.season.model.Season;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.web.client.HttpClientErrorException;

public class SeasonStepdefs extends BaseIntegrationTest {

  private Integer startYear;
  private Season seasonCreated;
  private Season seasonRetrieved;
  private HttpClientErrorException exception;

  @Before
  public void before() {
    super.before();
    startYear = null;
    seasonCreated = null;
    seasonRetrieved = null;
    exception = null;
  }

  @Given("^season starts encompassing today$")
  public void seasonStarts() throws Exception {
    // Arrange
    startYear = getSeasonStart().getYear();
  }

  @When("^the season is created$")
  public void the_season_is_created() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

  @When("^attempting to create the season$")
  public void attempting_to_create_the_season() throws Exception {
//    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
//    try {
//      seasonCreated = createSeason(start, token);
//    } catch (HttpClientErrorException e) {
//      exception = e;
//    }
  }

  @Then("^the start date should be May first$")
  public void verifyStartDate() throws Exception {
    assertEquals(getSeasonStart(), seasonCreated.getStart());
  }

  @Given("^season start date is missing$")
  public void season_start_date_is_missing() throws Exception {
    // Arrange
    startYear = null;
  }

  @Then("^response is \"([^\"]*)\"$")
  public void response_is(String expected) throws Exception {
    assertEquals(expected, exception.getStatusCode().toString());
  }

  @And("^the season is retrieved$")
  public void the_season_is_retrieved() throws Exception {
//    String token = login(USER_EMAIL, USER_PASSWORD);
//    seasonRetrieved = getSeason(seasonCreated.getId(), token);
  }

  @And("^the season costs should be set$")
  public void verifySeasonCosts() throws Exception {
    Assert.assertEquals(KITTY_PER_GAME, seasonCreated.getKittyPerGame());
    Assert.assertEquals(TOC_PER_GAME, seasonCreated.getTocPerGame());
    Assert.assertEquals(QUARTERLY_TOC_PER_GAME, seasonCreated.getQuarterlyTocPerGame());
    Assert.assertEquals(QUARTERLY_NUM_PAYOUTS, seasonCreated.getQuarterlyNumPayouts());
    Assert.assertEquals(GAME_BUY_IN, seasonCreated.getBuyInCost());
    Assert.assertEquals(GAME_REBUY, seasonCreated.getRebuyAddOnCost());
    Assert.assertEquals(GAME_REBUY_TOC_DEBIT, seasonCreated.getRebuyAddOnTocDebitCost());

    Assert.assertTrue(seasonCreated.getNumGames() == 52 || seasonCreated.getNumGames() == 53);
    Assert.assertFalse(seasonCreated.isFinalized());
  }
}

