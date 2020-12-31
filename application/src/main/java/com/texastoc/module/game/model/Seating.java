package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Seating {
  @Id
  private int id;
  private int gameId;
  private List<SeatsPerTable> seatsPerTables;
  private List<TableRequest> tableRequests;
  private List<GameTable> gameTables;
}
