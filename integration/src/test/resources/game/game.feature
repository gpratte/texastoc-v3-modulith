Feature: CRUD Games
  Create, Retrieve, Update (start, end) and Delete games

  Scenario: create a simple game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created
    Then the game is normal
    Then the game is not transport required
    Then after game scenario

  Scenario: game requires transport supplies
    Given before game scenario
    Given a season exists
    Given the game supplies need to be moved
    When the game is created
    Then the game transport supplies flag is set
    Then after game scenario

  Scenario: create and retrieve a simple game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created and retrieved
    Then the retrieved game is normal
    Then the retrieved game has no players or payouts
    Then after game scenario

  Scenario: create and retrieve the current game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created
    When the current game is retrieved
    Then the current game has no players or payouts
    Then after game scenario

  Scenario: create and update a simple game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created and retrieved
    And the retrieved game is updated and retrieved
    Then the game is normal
    Then after game scenario

  Scenario: create and finalize a simple game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created
    When the game is finalized
    And the current game is retrieved
    Then the retrieved game is finalized
    Then after game scenario

  Scenario: create, finalize and unfinalize a simple game
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created
    And the current game is retrieved
    Then the retrieved game is unfinalized
    When the game is finalized
    And the current game is retrieved
    Then the retrieved game is finalized
    When the game is unfinalized
    And the current game is retrieved
    Then the retrieved game is unfinalized
    Then after game scenario

  Scenario: try to create a game when there is a game in progress
    Given before game scenario
    Given a season exists
    Given the game starts now
    When the game is created
    When another game is created
    Then the new game is not allowed
    Then after game scenario
