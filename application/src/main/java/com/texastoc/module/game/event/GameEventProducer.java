package com.texastoc.module.game.event;

import com.texastoc.common.GameFinalizedEvent;

public class GameEventProducer {

  private final GameEventing gameEventing;

  public GameEventProducer(GameEventing gameEventing) {
    this.gameEventing = gameEventing;
  }

  public void notifyGameFinalized(int seasonId, int gameId, boolean finalized) {
    gameEventing.send(new GameFinalizedEvent(seasonId, gameId, finalized));
  }
}
