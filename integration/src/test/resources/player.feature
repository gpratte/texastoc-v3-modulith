Feature: Player features

  Scenario: Get player
    Given a new player
    When the player is retrieved
    Then the player matches

  Scenario: Get players
    Given a new player
    Given another new player
    When the players are retrieved
    Then the players match

  Scenario: Update player admin
    An admin updates another player
    Given a new player
    When the player is updated
    And the player is retrieved
    Then the updated player matches

  Scenario: Update another player non admin
    A non-admin attempts to update another player
    Given a new player
    When the player is updated by another player
    Then a forbidden error happens

  Scenario: Delete player admin
    An admin deletes a player
    Given a new player
    When the player is deleted
    And the player is retrieved
    Then a not found error happens

