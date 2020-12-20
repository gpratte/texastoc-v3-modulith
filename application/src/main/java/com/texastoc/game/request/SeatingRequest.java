package com.texastoc.game.request;

import com.texastoc.game.model.TableRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SeatingRequest {
  private List<Integer> numSeatsPerTable;
  private List<TableRequest> tableRequests;
}
