Feature: Play a game

  Scenario: start a game with one (empty) player
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

  Scenario: start a game with one player with everything set all
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

