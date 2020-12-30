package com.texastoc.module.game.service;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.GameTable;
import com.texastoc.module.game.model.Seat;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.model.TableRequest;
import com.texastoc.module.game.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SeatingService {

  private final GameRepository gameRepository;

  private final Random random = new Random(System.currentTimeMillis());

  public SeatingService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public Seating seatGamePlayers(Seating seating) {
    Optional<Game> optionalGame = gameRepository.findById(seating.getGameId());
    if (!optionalGame.isPresent()) {
      throw new NotFoundException("Game with id " + seating.getGameId() + " not found");
    }
    Game game = optionalGame.get();
    game.setSeating(seating);

    if (seating.getSeatsPerTables() == null) {
      seating.setSeatsPerTables(Collections.emptyList());
    }
    if (seating.getTableRequests() == null) {
      seating.setTableRequests(Collections.emptyList());
    }

    int numTables = seating.getSeatsPerTables().size();
    List<GameTable> gameTables = new ArrayList<>(numTables);
    seating.setGameTables(gameTables);

    if (numTables == 0) {
      gameRepository.save(game);
      return seating;
    }

    List<GamePlayer> currentPlayers = game.getPlayers();
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
      GameTable gameTable = GameTable.builder()
        .tableNum(i + 1)
        .build();
      gameTables.add(gameTable);
      List<Seat> seats = new ArrayList<>(seating.getSeatsPerTables().size());
      // All seats are dead stacks
      for (int j = 0; j < seating.getSeatsPerTables().size(); j++) {
        seats.add(null);
      }
      gameTable.setSeats(seats);
    }

    int totalPlayersRemaining = playersToRandomize.size();
    while ((totalPlayersRemaining) > 0) {
      // Add players in order
      for (GameTable gameTable : gameTables) {
        if (totalPlayersRemaining > 0) {
          // Get a player
          GamePlayer gamePlayer = playersToRandomize.get(totalPlayersRemaining - 1);
          totalPlayersRemaining -= 1;

          // Put the player at an empty seat
          List<Seat> seats = gameTable.getSeats();
          int seatIndex = 0;
          for (; seatIndex < seats.size(); seatIndex++) {
            if (seats.get(seatIndex) == null) {
              break;
            }
          }
          seats.set(seatIndex, Seat.builder()
            .seatNum(seatIndex + 1)
            .tableNum(gameTable.getTableNum())
            .gamePlayerId(gamePlayer.getId())
            .gamePlayerName(gamePlayer.getName())
            .build());
        }
      }
    }

    // Move the players around so there are dead stacks
    for (GameTable table : gameTables) {
      table.setSeats(spacePlayersWithDeadStacks(table.getSeats()));
    }

    // Go through the requests
    for (TableRequest tableRequest : seating.getTableRequests()) {

      // Find the seat of the player that wants to swap
      Seat playerThatWantsToSwapSeat = null;
      tableLoop:
      for (GameTable table : gameTables) {
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

      GameTable tableToMoveTo = gameTables.get(tableRequest.getTableNum() - 1);

      // See if player is already at that table
      if (playerThatWantsToSwapSeat.getTableNum() != tableToMoveTo.getTableNum()) {
        for (Seat seatAtTableToMoveTo : tableToMoveTo.getSeats()) {
          // Find a seat to swap - avoid seats that are dead stacks and avoid seats of players that have already been swapped
          if (seatAtTableToMoveTo.getGamePlayerId() != null && !seatBelongsToPlayerThatRequestedTheTable(seatAtTableToMoveTo.getGamePlayerId(), seating.getTableRequests())) {
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

    gameRepository.save(game);
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
          seat.setSeatNum(seatNumber);
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
        seat.setSeatNum(seatNumber);
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

  // TODO move to notifications
  public void notifySeating(int gameId) {
//    Seating seating = seatingRepository.get(gameId);
//    if (seating == null || seating.getTables() == null || seating.getTables().size() == 0) {
//      return;
//    }
//    for (Table table : seating.getTables()) {
//      if (table.getSeats() == null || table.getSeats().size() == 0) {
//        continue;
//      }
//      for (Seat seat : table.getSeats()) {
//        if (seat == null) {
//          continue;
//        }
//        GamePlayer gamePlayer = gamePlayerRepository.selectById(seat.getGamePlayerId());
//        Player player = getPlayerModule().get(gamePlayer.getPlayerId());
//        if (player.getPhone() != null) {
//          smsConnector.text(player.getPhone(), player.getName() + " table " +
//            table.getNumber() + " seat " + seat.getSeatNumber());
//        }
//      }
//    }
  }
}
