package com.texastoc.service;

import com.texastoc.TestConstants;
import com.texastoc.model.game.*;
import com.texastoc.repository.GamePlayerRepository;
import com.texastoc.repository.SeatingRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class SeatingServiceTest implements TestConstants {

  private SeatingService seatingService;

  @MockBean
  private SeatingRepository seatingRepository;
  @MockBean
  private GamePlayerRepository gamePlayerRepository;

  @Before
  public void before() {
    seatingService = new SeatingService(seatingRepository, gamePlayerRepository);
  }

  @Test
  public void testNotSeated() {

    Mockito.when(seatingRepository.get(1)).thenReturn(Seating.builder()
      .gameId(1)
      .numSeatsPerTable(Collections.emptyList())
      .tableRequests(Collections.emptyList())
      .tables(Collections.emptyList())
      .build());

    Seating seating = seatingService.get(1);

    Mockito.verify(seatingRepository, Mockito.times(1)).get(1);
    Assert.assertNotNull("seating should not be null", seating);
    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 0", 0, seating.getTables().size());
  }

  @Test
  public void testSeatNoPlayers() {
    Mockito.when(gamePlayerRepository.selectByGameId(1)).thenReturn(Collections.emptyList());

    Seating seating = seatingService.seat(1, null, null);

    Mockito.verify(gamePlayerRepository, Mockito.times(0)).selectByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).save(any(Seating.class));
    Assert.assertNotNull("seating should not be null", seating);
    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 0", 0, seating.getTables().size());
  }

  @Test
  public void testSeat2PlayersNoDeadStacks() {
    List<GamePlayer> gamePlayers = new ArrayList<>(2);
    gamePlayers.add(GamePlayer.builder()
      .id(1)
      .name("one")
      .buyInCollected(10)
      .build());
    gamePlayers.add(GamePlayer.builder()
      .id(2)
      .name("two")
      .buyInCollected(10)
      .build());
    Mockito.when(gamePlayerRepository.selectByGameId(1)).thenReturn(gamePlayers);

    List<Integer> numSeatsPerTable = Arrays.asList(2);

    Seating seating = seatingService.seat(1, numSeatsPerTable, null);

    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).save(any(Seating.class));

    Assert.assertNotNull("seating should not be null", seating);
    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 1", 1, seating.getTables().size());

    Table firstTable = seating.getTables().get(0);
    Assert.assertNotNull("seats for table 1 should not be null", firstTable.getSeats());
    Assert.assertEquals("seats for table 1 should be 2", 2, firstTable.getSeats().size());
  }

  @Test
  public void testSeat9Players3DeadStacks() {
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 9; i++) {
      gamePlayers.add(GamePlayer.builder()
        .id(i)
        .name("name" + i)
        .buyInCollected(10)
        .build());
    }

    Mockito.when(gamePlayerRepository.selectByGameId(1)).thenReturn(gamePlayers);

    List<Integer> numSeatsPerTable = Arrays.asList(6, 6);
    Seating seating = seatingService.seat(1, numSeatsPerTable, null);

    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);

    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).save(any(Seating.class));

    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 2", 2, seating.getTables().size());

    Table firstTable = seating.getTables().get(0);
    Assert.assertNotNull("seats for table 1 should not be null", firstTable.getSeats());
    Assert.assertEquals("seats for table 1 should be 6", 6, firstTable.getSeats().size());

    int countDeads = 0;
    for (Seat seat : firstTable.getSeats()) {
      if (seat == null) {
        ++countDeads;
      }
    }
    Assert.assertEquals("table 1 should have 1 dead stack", 1, countDeads);

    Table secondTable = seating.getTables().get(1);
    Assert.assertNotNull("seats for table 2 should not be null", secondTable.getSeats());
    Assert.assertEquals("seats for table 2 should be 6", 6, secondTable.getSeats().size());

    countDeads = 0;
    for (Seat seat : secondTable.getSeats()) {
      if (seat == null) {
        ++countDeads;
      }
    }
    Assert.assertEquals("table 2 should have 2 dead stacks", 2, countDeads);
  }

  @Test
  public void testSeat11Players6AtTable1() {

    List<GamePlayer> gamePlayers = new ArrayList<>(2);
    for (int i = 0; i < 11; i++) {
      gamePlayers.add(GamePlayer.builder()
        .id(i)
        .name("name" + i)
        .buyInCollected(10)
        .build());
    }

    Mockito.when(gamePlayerRepository.selectByGameId(1)).thenReturn(gamePlayers);

    List<TableRequest> tableRequests = new ArrayList<>(6);
    for (int i = 0; i < 6; i++) {
      tableRequests.add(TableRequest.builder()
        .gamePlayerId(i)
        .tableNum(1)
        .build());
    }

    List<Integer> numSeatsPerTable = Arrays.asList(7, 7);
    Seating seating = seatingService.seat(1, numSeatsPerTable, tableRequests);

    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);

    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).save(Mockito.any(Seating.class));

    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 2", 2, seating.getTables().size());

    Table firstTable = seating.getTables().get(0);
    Assert.assertNotNull("seats for table 1 should not be null", firstTable.getSeats());
    Assert.assertEquals("seats for table 1 should be 7", 7, firstTable.getSeats().size());

    for (int i = 0; i < 6; i++) {
      boolean found = false;
      for (Seat seat : firstTable.getSeats()) {
        if (seat.getGamePlayerId() == i) {
          found = true;
          break;
        }
      }
      Assert.assertTrue("should have found game player " + i + " at table 1", found);
    }
  }

  @Test
  public void testSeat17Players2DeadStacks() {
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 17; i++) {
      gamePlayers.add(GamePlayer.builder()
        .id(i)
        .name("name" + i)
        .buyInCollected(10)
        .build());
    }

    Mockito.when(gamePlayerRepository.selectByGameId(1)).thenReturn(gamePlayers);

    List<Integer> numSeatsPerTable = Arrays.asList(7, 6, 6);
    Seating seating = seatingService.seat(1, numSeatsPerTable, Collections.emptyList());

    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);

    Mockito.verify(seatingRepository, Mockito.times(1)).deleteByGameId(1);
    Mockito.verify(seatingRepository, Mockito.times(1)).save(any(Seating.class));

    Assert.assertNotNull("tables should not be null", seating.getTables());
    Assert.assertEquals("number of tables 3", 3, seating.getTables().size());

    Table firstTable = seating.getTables().get(0);
    Assert.assertNotNull("seats for table 1 should not be null", firstTable.getSeats());
    Assert.assertEquals("seats for table 1 should be 7", 7, firstTable.getSeats().size());

    int countDeads = 0;
    for (Seat seat : firstTable.getSeats()) {
      if (seat == null) {
        ++countDeads;
      }
    }
    Assert.assertEquals("table 1 should have 1 dead stack", 1, countDeads);

    Table secondTable = seating.getTables().get(1);
    Assert.assertNotNull("seats for table 2 should not be null", secondTable.getSeats());
    Assert.assertEquals("seats for table 2 should be 6", 6, secondTable.getSeats().size());

    countDeads = 0;
    for (Seat seat : secondTable.getSeats()) {
      if (seat == null) {
        ++countDeads;
      }
    }
    Assert.assertEquals("table 2 should have 0 dead stacks", 0, countDeads);

    Table thirdTable = seating.getTables().get(2);
    Assert.assertNotNull("seats for table 3 should not be null", thirdTable.getSeats());
    Assert.assertEquals("seats for table 3 should be 6", 6, thirdTable.getSeats().size());

    countDeads = 0;
    for (Seat seat : thirdTable.getSeats()) {
      if (seat == null) {
        ++countDeads;
      }
    }
    Assert.assertEquals("table 3 should have 1 dead stack", 1, countDeads);

  }
}
