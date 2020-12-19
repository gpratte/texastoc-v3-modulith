Feature: Finalizing a game results in calculations

  Scenario: a game is calculated
    Given a game has 10 players all finished
    When the game is finalized
    And the calculated season is retrieved
    Then the game is properly calculated

  Scenario: a quarterly season is calculated
    Given a game has 10 players all finished
    When the game is finalized
    And the calculated season is retrieved
    Then the quarterly seasons are properly calculated

  Scenario: a season is calculated
    Given a game has 10 players all finished
    When the game is finalized
    And the calculated season is retrieved
    Then the season is properly calculated
