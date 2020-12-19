Feature: Add players to a game

  Scenario: add player no buy-in and retrieve game
    Given a game is created
    And a player is added without buy-in
    And the game is retrieved
    Then the retrieved game has one player no buy-in
    And paid players is 0
    And paid players remaining is 0

  Scenario: add player with buy-in and retrieve game
    Given a game is created
    And a player is added with buy-in
    And the game is retrieved
    Then the retrieved game has one player with buy-in
    And paid players is 1
    And paid players remaining is 1

  Scenario: add 2 players with buy-in and retrieve game
    Given a game is created
    And two players are added with buy-in
    And the game is retrieved
    Then the retrieved game has two players with buy-in
    And paid players is 2
    And paid players remaining is 2

  Scenario: add random players and retrieve game
    Given a game is created
    And random players are added
    And the game is retrieved
    Then the retrieved game has random players

  Scenario: add player and update player
    Given a game is created
    And a player is added without buy-in
    And the player is updated
    And the game is retrieved
    Then the retrieved game has one player with updates
    And paid players is 1
    And paid players remaining is 1

  Scenario: add player and knock out the player
    Given a game is created
    And a player is added with buy-in
    And the player is knocked out
    And the game is retrieved
    Then the retrieved game has one player with updates
    And paid players is 1
    And paid players remaining is 0

  Scenario: add player and delete player
    Given a game is created
    And a player is added with buy-in
    And the player is deleted
    And the game is retrieved
    Then the retrieved game does not have the player
    And paid players is 0
    And paid players remaining is 0

  Scenario: add first time player
    Given a game is created
    And a first time player is added
    And the game is retrieved
    Then the retrieved game has the first time player
