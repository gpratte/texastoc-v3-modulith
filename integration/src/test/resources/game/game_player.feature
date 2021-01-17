Feature: Add players to a game

  Scenario: add empty existing player
    Add an existing player as a game player with minimal fields set
    Given before game player scenario
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved game players have nothing set
    Then after game player scenario

  Scenario: add existing player with all fields set
    Add an existing player as a game player with all fields set
    Given before game player scenario
    Given a game is created
    And a player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved game players have everything set
    Then after game player scenario

  Scenario: add empty first time player
    Add a first time player as a game player with minimal fields set
    Given before game player scenario
    Given a game is created
    And a first time player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved first time game players have nothing set
    Then after game player scenario

  Scenario: add empty first time player with everything set
    Add a first time player as a game player with everything set
    Given before game player scenario
    Given a game is created
    And a first time player is added with everything set
    When the game is updated with the players
    And the current players are retrieved
    Then the retrieved first time game players have everything set
    Then after game player scenario

  Scenario: update player
    Add an existing player as a game player with minimal fields set and then update all fields
    Given before game player scenario
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    And the current players are updated
    When the game is updated with the updated players
    Then the retrieved game players have everything set
    Then after game player scenario

  Scenario: knock out a player
    Given before game player scenario
    Given a game is created
    And a player is added with nothing set
    When the game is updated with the players
    And the current players are retrieved
    And the current players are knocked out
    When the game is updated with the updated players
    Then the retrieved game players are knocked out
    Then after game player scenario

#  Scenario: add player and delete player
#    Given a game is created
#    And a player is added with buy-in
#    And the player is deleted
#    And the game is retrieved
#    Then the retrieved game does not have the player
#    And paid players is 0
#    And paid players remaining is 0
#
#  Scenario: add first time player
#    Given a game is created
#    And a first time player is added
#    And the game is retrieved
#    Then the retrieved game has the first time player
