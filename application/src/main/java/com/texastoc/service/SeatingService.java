package com.texastoc.service;

import com.texastoc.model.game.*;
import com.texastoc.repository.GamePlayerRepository;
import com.texastoc.repository.SeatingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class SeatingService {

  private final SeatingRepository seatingRepository;
  private final GamePlayerRepository gamePlayerRepository;

  private final Random random = new Random(System.currentTimeMillis());

  public SeatingService(SeatingRepository seatingRepository, GamePlayerRepository gamePlayerRepository) {
    this.seatingRepository = seatingRepository;
    this.gamePlayerRepository = gamePlayerRepository;
  }

  public Seating get(int gameId) {
    return seatingRepository.get(gameId);
  }

  public Seating seat(int gameId, List<Integer> numSeatsPerTable, List<TableRequest> tableRequests) {
    numSeatsPerTable = numSeatsPerTable == null ? Collections.emptyList() : numSeatsPerTable;
    tableRequests = tableRequests == null ? Collections.emptyList() : tableRequests;
    int numTables = numSeatsPerTable.size();
    List<Table> tables = new ArrayList<>(numTables);
    Seating seating = Seating.builder()
      .gameId(gameId)
      .numSeatsPerTable(numSeatsPerTable)
      .tableRequests(tableRequests)
      .tables(tables)
      .build();

    if (numTables == 0) {
      seatingRepository.deleteByGameId(gameId);
      seatingRepository.save(seating);
      return seating;
    }

    List<GamePlayer> currentPlayers = gamePlayerRepository.selectByGameId(gameId);
    // Count the players that are in the game and have a buy in
    int numPlayersWithBuyIns = 0;
    for (GamePlayer gamePlayer : currentPlayers) {
      if (gamePlayer.getBuyInCollected() != null && gamePlayer.getBuyInCollected() > 0) {
        ++numPlayersWithBuyIns;
      }
    }

    // Players that are in the game and have a buy in
    List<GamePlayer> playersToRandomize = new ArrayList<>(numPlayersWithBuyIns);
    for (GamePlayer gamePlayer : currentPlayers) {
      if (gamePlayer.getBuyInCollected() != null && gamePlayer.getBuyInCollected() > 0) {
        playersToRandomize.add(gamePlayer);
      }
    }

    if (playersToRandomize.size() > 2) {
      // shuffle the players
      Random random = new Random(System.currentTimeMillis());
      for (int loop = 0; loop < 10; ++loop) {
        Collections.shuffle(playersToRandomize, random);
      }
    }

    // Create the seats for the tables (tables are numbered 1's based)
    for (int i = 0; i < numTables; i++) {
      Table table = Table.builder()
        .number(i + 1)
        .gameId(gameId)
        .build();
      tables.add(table);
      List<Seat> seats = new ArrayList<>(numSeatsPerTable.get(i));
      // All seats are dead stacks
      for (int j = 0; j < numSeatsPerTable.get(i); j++) {
        seats.add(null);
      }
      table.setSeats(seats);
    }

    int totalPlayersRemaining = playersToRandomize.size();
    while ((totalPlayersRemaining) > 0) {
      // Add players in order
      for (Table table : tables) {
        if (totalPlayersRemaining > 0) {
          // Get a player
          GamePlayer gamePlayer = playersToRandomize.get(totalPlayersRemaining - 1);
          totalPlayersRemaining -= 1;

          // Put the player at an empty seat
          List<Seat> seats = table.getSeats();
          int seatIndex = 0;
          for (; seatIndex < seats.size(); seatIndex++) {
            if (seats.get(seatIndex) == null) {
              break;
            }
          }
          seats.set(seatIndex, Seat.builder()
            .gameId(gameId)
            .seatNumber(seatIndex + 1)
            .tableNumber(table.getNumber())
            .gamePlayerId(gamePlayer.getId())
            .gamePlayerName(gamePlayer.getName())
            .build());
        }
      }
    }

    // Move the players around so there are dead stacks
    for (Table table : tables) {
      table.setSeats(spacePlayersWithDeadStacks(table.getSeats()));
    }

    // Go through the requests
    for (TableRequest tableRequest : tableRequests) {

      // Find the seat of the player that wants to swap
      Seat playerThatWantsToSwapSeat = null;
      tableLoop:
      for (Table table : tables) {
        for (Seat seat : table.getSeats()) {
          if (seat != null && seat.getGamePlayerId() != null && seat.getGamePlayerId() == tableRequest.getGamePlayerId()) {
            playerThatWantsToSwapSeat = seat;
            break tableLoop;
          }
        }
      }

      if (playerThatWantsToSwapSeat == null) {
        // should never happen
        continue;
      }

      Table tableToMoveTo = tables.get(tableRequest.getTableNum() - 1);

      // See if player is already at that table
      if (playerThatWantsToSwapSeat.getTableNumber() != tableToMoveTo.getNumber()) {
        for (Seat seatAtTableToMoveTo : tableToMoveTo.getSeats()) {
          // Find a seat to swap - avoid seats that are dead stacks and avoid seats of players that have already been swapped
          if (seatAtTableToMoveTo.getGamePlayerId() != null && !seatBelongsToPlayerThatRequestedTheTable(seatAtTableToMoveTo.getGamePlayerId(), tableRequests)) {
            // Swap
            int saveGamePlayerId = seatAtTableToMoveTo.getGamePlayerId();
            String saveGamePlayerName = seatAtTableToMoveTo.getGamePlayerName();

            seatAtTableToMoveTo.setGamePlayerId(playerThatWantsToSwapSeat.getGamePlayerId());
            seatAtTableToMoveTo.setGamePlayerName(playerThatWantsToSwapSeat.getGamePlayerName());

            playerThatWantsToSwapSeat.setGamePlayerId(saveGamePlayerId);
            playerThatWantsToSwapSeat.setGamePlayerName(saveGamePlayerName);
            break;
          }
        }
      }
    }

    seatingRepository.deleteByGameId(gameId);
    seatingRepository.save(seating);
    return seating;
  }

  List<Seat> spacePlayersWithDeadStacks(List<Seat> seats) {
    // Some sanity checking
    if (seats == null || seats.size() == 0 || seats.get(0) == null) {
      return seats;
    }

    int numSeats = seats.size();
    int numPlayers = (int) seats.stream()
      .filter(seat -> seat != null)
      .count();

    if (numPlayers == numSeats || numPlayers == (numSeats - 1)) {
      return seats;
    }

    int numDeadStacks = numSeats - numPlayers;

    if (numPlayers >= numDeadStacks) {
      // alternate player and dead stack
      List<Seat> newSeats = new ArrayList<>(numSeats);
      int numDeadStacksSeated = 0;
      int seatNumber = 1;
      for (int i = 0; i < seats.size(); i++) {
        Seat seat = seats.get(i);
        if (seat != null) {
          seat.setSeatNumber(seatNumber);
          newSeats.add(seat);
          if (++numDeadStacksSeated <= numDeadStacks) {
            ++seatNumber;
            newSeats.add(null);
          }
        }
      }
      return newSeats;
    }

    int numDeadsBetween = numDeadStacks / numPlayers;
    List<Seat> newSeats = new ArrayList<>(numSeats);
    int seatNumber = 1;
    for (int i = 0; i < seats.size(); i++) {
      Seat seat = seats.get(i);
      if (seat != null) {
        seat.setSeatNumber(seatNumber);
        newSeats.add(seat);
        for (int j = 0; j < numDeadsBetween; j++) {
          ++seatNumber;
          newSeats.add(null);
        }
      }
    }
    return newSeats;
  }

  private boolean seatBelongsToPlayerThatRequestedTheTable(int gamePlayerId, List<TableRequest> tableRequests) {
    for (TableRequest tableRequest : tableRequests) {
      if (tableRequest.getGamePlayerId() == gamePlayerId) {
        return true;
      }
    }
    return false;
  }
}
