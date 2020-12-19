package com.texastoc.cucumber;

import com.texastoc.controller.request.CreateGameRequest;
import com.texastoc.model.game.*;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Ignore;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class SeatingStepdefs extends SpringBootBaseIntegrationTest {

  Integer gameId;
  Seating seating;

  @Before
  public void before() {
    gameId = null;
    seating = null;
  }

  @Given("^a game has (\\d+) players$")
  public void aGameHasPlayers(int numPlayers) throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    createSeason(getSeasonStart(), token);

    Game game = createGame(CreateGameRequest.builder()
      .date(LocalDate.now())
      .hostId(1)
      .transportRequired(false)
      .build(), token);

    gameId = game.getId();

    token = login(USER_EMAIL, USER_PASSWORD);

    for (int i = 0; i < numPlayers; i++) {
      FirstTimeGamePlayer firstTimeGamePlayer = FirstTimeGamePlayer.builder()
        .firstName("Joe" + i)
        .lastName("Schmoe")
        .email("joe" + i + ".schmoe@texastoc.com")
        .gameId(gameId)
        .buyInCollected(true)
        .build();
      addFirstTimePlayerToGame(firstTimeGamePlayer, token);
    }
  }

  @When("^seating is done with (\\d+) and (\\d+) seats$")
  public void seatingIsDonwWithAndSeats(int seatsAtTable1, int seatsAtTable2) throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    List<Integer> numSeatsPerTable = new LinkedList<>();
    if (seatsAtTable1 > 0) {
      numSeatsPerTable.add(seatsAtTable1);
    }
    if (seatsAtTable2 > 0) {
      numSeatsPerTable.add(seatsAtTable2);
    }
    seating = seatPlayers(gameId, numSeatsPerTable, null, token);
  }

  @Then("^(\\d+) players are seated at table (\\d+)$")
  public void playersAreSeatedAtTable(int numPlayers, int tableNum) throws Exception {
    List<Table> tables = seating.getTables();
    assertNotNull("tables should not be null", tables);

    Table table = tables.get(tableNum - 1);
    assertNotNull("seats should not be null", table.getSeats());

    List<Seat> seats = table.getSeats();
    int numPlayersSeated = (int) seats.stream()
      .filter((seat) -> seat != null)
      .count();
    assertEquals(numPlayers + " are seated", numPlayers, numPlayersSeated);
  }

  @And("^table (\\d+) has (\\d+) dead stacks$")
  public void tableHasDeadStacks(int tableNum, int numDeadStacks) throws Exception {
    List<Table> tables = seating.getTables();
    Table table = tables.get(tableNum - 1);
    List<Seat> seats = table.getSeats();
    int numEmptySeats = (int) seats.stream()
      .filter((seat) -> seat == null)
      .count();
    assertEquals("should have " + numDeadStacks + " dead stacks", numDeadStacks, numEmptySeats);
  }
}
