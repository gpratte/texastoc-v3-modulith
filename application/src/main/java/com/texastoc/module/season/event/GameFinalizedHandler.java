package com.texastoc.module.season.event;

import com.texastoc.common.GameFinalizedEvent;

public class GameFinalizedHandler {

  public void handleGameFinalized(GameFinalizedEvent event) {
    System.out.println("!!! season got game finalized event " + event);
  }
}
