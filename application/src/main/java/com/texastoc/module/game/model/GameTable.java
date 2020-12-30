package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameTable {
  private int id;
  private int tableNum;
  private List<Seat> seats;

  public void addSeat(Seat seat) {
    if (seats == null) {
      seats = new ArrayList<>();
    }
    seats.add(seat);
  }
}
