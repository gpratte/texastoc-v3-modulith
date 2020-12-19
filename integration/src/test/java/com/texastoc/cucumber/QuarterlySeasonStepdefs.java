package com.texastoc.cucumber;

import com.texastoc.model.season.QuarterlySeason;
import com.texastoc.model.season.Season;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;

import java.time.LocalDate;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class QuarterlySeasonStepdefs extends SpringBootBaseIntegrationTest {

  private LocalDate start;
  private Season seasonCreated;

  @Given("^first quarterly season starts now$")
  public void season_starts_now() throws Exception {
    // Arrange
    start = getSeasonStart();
  }

  @When("^the quarterly seasons are created$")
  public void the_season_is_created() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(start, token);
  }

  @Then("^four quarterly seasons should be created$")
  public void the_start_date_should_be_now() throws Exception {
    Assert.assertTrue(seasonCreated.getQuarterlySeasons().size() == 4);

    for (int i = 0; i < 4; ++i) {
      QuarterlySeason qSeason = seasonCreated.getQuarterlySeasons().get(i);
      Assert.assertTrue(qSeason.getId() > 0);
      Assert.assertEquals((int) i + 1, (int) qSeason.getQuarter().getValue());

      Assert.assertEquals(QUARTERLY_TOC_PER_GAME, (int) qSeason.getQTocPerGame());
      Assert.assertEquals(QUARTERLY_NUM_PAYOUTS, (int) qSeason.getNumPayouts());

      Assert.assertTrue(qSeason.getQTocCollected() == 0);

      Assert.assertTrue(qSeason.getNumGamesPlayed() == 0);
      Assert.assertTrue(qSeason.getNumGames() == 12 || qSeason.getNumGames() == 13 || qSeason.getNumGames() == 14);

      Assert.assertTrue(qSeason.getPlayers() == null || qSeason.getPlayers().size() == 0);
      Assert.assertTrue(qSeason.getPayouts() == null || qSeason.getPayouts().size() == 0);
    }

  }
}
