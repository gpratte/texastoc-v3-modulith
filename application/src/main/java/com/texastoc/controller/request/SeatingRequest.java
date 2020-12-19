package com.texastoc.controller.request;

import com.texastoc.model.game.TableRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SeatingRequest {
  private List<Integer> numSeatsPerTable;
  private List<TableRequest> tableRequests;
}
