Feature: Player features

  Scenario: Login
    Given a new player with email and password
    When the player logs in
    Then a token is returned

  Scenario: Get player
    Given a new player
    When the player is retrieved
    Then the player matches

  Scenario: Get players
    Given a new player
    Given another new player
    When the players are retrieved
    Then the players match

  Scenario: Update password
    Given a new player
    When the player password is updated
    And the player self retrieves
    Then the player has the expected encoded password
