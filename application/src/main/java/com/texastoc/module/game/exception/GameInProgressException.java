package com.texastoc.module.game.exception;

public class GameInProgressException extends RuntimeException {

  public GameInProgressException(String message) {
    super(message);
  }
}
