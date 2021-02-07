Feature: Play a game

  Scenario: game with one (empty) player
    Given a calculated game is created
    When adding players
    """
[
  {
    "boughtIn":false,
    "annualTocParticipant":false,
    "quarterlyTocParticipant":false,
    "rebought":false,
    "place":null,
    "chop":null
  }
]
    """
    And the current calculated game is retrieved
    Then the game calculated is
    """
{
  "buyInCollected":0,
  "rebuyAddOnCollected":0,
  "annualTocCollected":0,
  "quarterlyTocCollected":0,
  "totalCollected":0,
  "annualTocFromRebuyAddOnCalculated":0,
  "rebuyAddOnLessAnnualTocCalculated":0,
  "totalCombinedTocCalculated":0,
  "kittyCalculated":0,
  "prizePotCalculated":0,
  "numPlayers":1,
  "numPaidPlayers":0,
  "chopped":false,
  "canRebuy":true,
  "finalized":false,
  "payouts":[]
}
    """

  Scenario: game with one player with everything set all
  The player is bought-in, rebought, annual toc participant,
  quarterly toc participant and is in first place
    Given a calculated game is created
    When adding players
    """
[
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":1,
    "chop":null
  }
]
    """
    And the current calculated game is retrieved
    Then the game calculated is
    """
{
  "buyInCollected":40,
  "rebuyAddOnCollected":40,
  "annualTocCollected":20,
  "quarterlyTocCollected":20,
  "totalCollected":120,
  "annualTocFromRebuyAddOnCalculated":20,
  "rebuyAddOnLessAnnualTocCalculated":20,
  "totalCombinedTocCalculated":60,
  "kittyCalculated":10,
  "prizePotCalculated":50,
  "numPlayers":1,
  "numPaidPlayers":1,
  "chopped":false,
  "canRebuy":true,
  "payouts":[
    {
      "place":1,
      "amount":50,
      "chopAmount":null
    }
  ]
}
    """

  Scenario: game with ten players
  Game over with ten players with all but one bought-in, 7 have rebought,
  6 are annual toc participants, 3 are quarterly toc participants
  and top 2 chopped the pot
    Given a calculated game is created
    When adding players
    """
[
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":false,
    "place":5,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":false,
    "rebought":true,
    "place":4,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":false,
    "rebought":true,
    "place":9,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":false,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":7,
    "chop":null
  },
  {
    "boughtIn":false,
    "annualTocParticipant":false,
    "quarterlyTocParticipant":false,
    "rebought":false,
    "place":null,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":6,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":false,
    "rebought":false,
    "place":1,
    "chop":55000
  },
  {
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":false,
    "rebought":true,
    "place":2,
    "chop":48750
  },
  {
    "boughtIn":true,
    "annualTocParticipant":false,
    "quarterlyTocParticipant":false,
    "rebought":true,
    "place":8,
    "chop":null
  },
  {
    "boughtIn":true,
    "annualTocParticipant":false,
    "quarterlyTocParticipant":false,
    "rebought":true,
    "place":3,
    "chop":null
  }
]
    """
    And the current calculated game is retrieved
    Then the game calculated is
    """
{
  "buyInCollected":360,
  "rebuyAddOnCollected":280,
  "annualTocCollected":120,
  "quarterlyTocCollected":60,
  "totalCollected":820,
  "annualTocFromRebuyAddOnCalculated":80,
  "rebuyAddOnLessAnnualTocCalculated":200,
  "totalCombinedTocCalculated":260,
  "kittyCalculated":10,
  "prizePotCalculated":550,
  "numPlayers":10,
  "numPaidPlayers":9,
  "chopped":true,
  "canRebuy":true,
  "payouts":[
    {
      "place":1,
      "amount":357,
      "chopAmount":280
    },
    {
      "place":2,
      "amount":193,
      "chopAmount":270
    }
  ]
}
    """

