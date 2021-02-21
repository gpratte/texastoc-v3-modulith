Feature: a finalized game triggers the season to recalculate

  Scenario: calculate a season with one game
    Given a season started encompassing today
    And a running game has players
    """
[
  {
    "firstName":"abe",
    "lastName":"abeson",
    "boughtIn":true,
    "annualTocParticipant":true,
    "quarterlyTocParticipant":true,
    "rebought":true,
    "place":1,
    "chop":null
  }
]
    """
    When the finalized game triggers the season to recalculate
    Then the calculated season is retrieved with 1 games played
    Then the season calculations should be
    """
{
  "buyInCollected":40,
  "rebuyAddOnCollected":40,
  "annualTocCollected":20,
  "totalCollected":120,
  "annualTocFromRebuyAddOnCalculated":20,
  "rebuyAddOnLessAnnualTocCalculated":20,
  "totalCombinedAnnualTocCalculated":40,
  "kittyCalculated":10,
  "prizePotCalculated":50,
  "numGames":52,
  "numGamesPlayed":1,
  "finalized":false,
  "players":[
    {
      "name":"abe abeson",
      "place":1,
      "points":30,
      "entries":1
    }
  ],
  "payouts":[],
  "estimatedPayouts":[]
}
    """
