Feature: CRUD seasons
  Create, Retrieve, Update and Delete seasons

  Scenario: create a season
    Given season starts now
    When the season is created
    Then the start date should be now

  Scenario: validate season start required
    Given season start date is missing
    When attempting to create the season
    Then response is "400 BAD_REQUEST"

  Scenario: get a season with no games
    Given season starts now
    When the season is created
    And the season is retrieved
    Then the season should have four quarters
