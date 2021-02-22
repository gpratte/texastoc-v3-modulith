package com.texastoc.module.quarterly;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.season.model.Season;
import org.junit.Before;

public class BaseQuarterlySeasonStepdefs extends BaseIntegrationTest {

  protected Integer startYear;
  protected Season seasonCreated;

  @Before
  public void before() {
    super.before();
    startYear = null;
    seasonCreated = null;
  }

}

