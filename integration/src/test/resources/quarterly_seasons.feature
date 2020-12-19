Feature: CRUD quarterly seasons
  Create, Retrieve, Update and Delete quarterly seasons

  Scenario: create a season
    Given first quarterly season starts now
    When the quarterly seasons are created
    Then four quarterly seasons should be created

