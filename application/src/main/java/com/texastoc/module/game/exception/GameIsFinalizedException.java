package com.texastoc.module.game.exception;

public class GameIsFinalizedException extends RuntimeException {

  public GameIsFinalizedException(String message) {
    super(message);
  }
}
