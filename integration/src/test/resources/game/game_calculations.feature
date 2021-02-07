Feature: Play a game

  Scenario Outline: start a game
    Given a calculated game is created
    When a player is added without buy-in
    And the current calculated game is retrieved
    Then the game calculated is <game>

    Examples:
      | game                                                                                                                                                                                                                                                                                                                                                                      |
      | {"buyInCollected":0,"rebuyAddOnCollected":0,"annualTocCollected":0,"quarterlyTocCollected":0,"totalCollected":0,"annualTocFromRebuyAddOnCalculated":0,"rebuyAddOnLessAnnualTocCalculated":0,"totalCombinedTocCalculated":0,"kittyCalculated":0,"prizePotCalculated":0,"numPlayers":1,"numPaidPlayers":0,"chopped":false,"canRebuy":true,"finalized":false,"payouts":null} |

#  Scenario: add player no buy-in
#    Given a game is created
#    And a player is added without buy-in
#    And the current game is retrieved :gp
#    Then the retrieved game has one player no buy-in
#    And paid players is 0
#
#  Scenario: add player with buy-in
#    Given a game is created
#    And a player is added with buy-in
#    And the current game is retrieved :gp
#    Then the retrieved game has one player with buy-in
#    And paid players is 1
#
#  Scenario: add 2 players with buy-in
#    Given a game is created
#    And two players are added with buy-in
#    And the game is retrieved
#    Then the retrieved game has two players with buy-in
#    And paid players is 2
#    And paid players remaining is 2
#
#  Scenario: add random players
#    Given a game is created
#    And random players are added
#    And the game is retrieved
#    Then the retrieved game has random players
#
#  Scenario: add player and update player
#    Given a game is created
#    And a player is added without buy-in
#    And the player is updated
#    And the game is retrieved
#    Then the retrieved game has one player with updates
#    And paid players is 1
#    And paid players remaining is 1
#
#  Scenario: add player and knock out the player
#    Given a game is created
#    And a player is added with buy-in
#    And the player is knocked out
#    And the game is retrieved
#    Then the retrieved game has one player with updates
#    And paid players is 1
#    And paid players remaining is 0
#
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
