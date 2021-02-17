package com.texastoc.module.season;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.TestUtils;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.season.model.Season;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.web.client.HttpClientErrorException;

public class SeasonStepdefs extends BaseIntegrationTest {

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
//    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
//    seasonCreated = createSeason(start, token);
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
//    String token = login(USER_EMAIL, USER_PASSWORD);
//    seasonRetrieved = getSeason(seasonCreated.getId(), token);
  }

  @Then("^the season should have four quarters$")
  public void the_season_should_have_four_quarters() throws Exception {
    // TODO
//    Assert.assertNotNull("season retrieved should not be null", seasonRetrieved);
//    Assert.assertNotNull("season retrieved quarterly seasons should not be null", seasonRetrieved.getQuarterlySeasons());
//    Assert.assertEquals(4, seasonRetrieved.getQuarterlySeasons().size());
//    Assert.assertNotNull("games should not be null", seasonRetrieved.getGames());
//    Assert.assertEquals("season should have 0 games", 0, seasonRetrieved.getGames().size());
  }

}
