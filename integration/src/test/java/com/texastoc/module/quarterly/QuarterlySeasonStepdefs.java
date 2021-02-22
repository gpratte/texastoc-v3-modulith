package com.texastoc.module.quarterly;

import com.texastoc.module.quarterly.model.QuarterlySeason;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.junit.Before;

public class QuarterlySeasonStepdefs extends BaseQuarterlySeasonStepdefs {

  @Before
  public void before() {
    super.before();
  }

  @Given("^the season start year encompassing today$")
  public void seasonStarts() throws Exception {
    startYear = getSeasonStart().getYear();
  }

  @When("^the current season is created$")
  public void createTheSeason() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(startYear, token);
  }

  @Then("^four quarterly seasons should be created$")
  public void verifyQuarters() throws Exception {
    List<QuarterlySeason> quarters =
  }


}
