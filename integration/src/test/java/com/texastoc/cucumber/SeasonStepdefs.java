package com.texastoc.cucumber;

import com.texastoc.TestUtils;
import com.texastoc.model.game.Game;
import com.texastoc.model.season.Season;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class SeasonStepdefs extends SpringBootBaseIntegrationTest {

  private LocalDate start;
  private Season seasonCreated;
  private Season seasonRetrieved;
  private List<Game> games = new ArrayList<>();
  private HttpClientErrorException exception;

  @Before
  public void before() {
    start = null;
    seasonCreated = null;
    seasonRetrieved = null;
    exception = null;
    games.clear();
  }

  @Given("^season starts now$")
  public void season_starts_now() throws Exception {
    // Arrange
    start = getSeasonStart();
  }

  @When("^the season is created$")
  public void the_season_is_created() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(start, token);
  }

  @When("^attempting to create the season$")
  public void attempting_to_create_the_season() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    try {
      seasonCreated = createSeason(start, token);
    } catch (HttpClientErrorException e) {
      exception = e;
    }
  }

  @Then("^the start date should be now$")
  public void the_start_date_should_be_now() throws Exception {
    TestUtils.assertCreatedSeason(start, seasonCreated);
  }

  @Given("^season start date is missing$")
  public void season_start_date_is_missing() throws Exception {
    // Arrange
    start = null;
  }

  @Then("^response is \"([^\"]*)\"$")
  public void response_is(String expected) throws Exception {
    Assert.assertEquals(expected, exception.getStatusCode().toString());
  }

  @And("^the season is retrieved$")
  public void the_season_is_retrieved() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    seasonRetrieved = getSeason(seasonCreated.getId(), token);
  }

  @Then("^the season should have four quarters$")
  public void the_season_should_have_four_quarters() throws Exception {
    Assert.assertNotNull("season retrieved should not be null", seasonRetrieved);
    Assert.assertNotNull("season retrieved quarterly seasons should not be null", seasonRetrieved.getQuarterlySeasons());
    Assert.assertEquals(4, seasonRetrieved.getQuarterlySeasons().size());
    Assert.assertNotNull("games should not be null", seasonRetrieved.getGames());
    Assert.assertEquals("season should have 0 games", 0, seasonRetrieved.getGames().size());
  }

}
