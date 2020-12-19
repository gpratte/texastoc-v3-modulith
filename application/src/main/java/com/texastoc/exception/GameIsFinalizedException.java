package com.texastoc.exception;

public class GameIsFinalizedException extends RuntimeException {

  public GameIsFinalizedException(String message) {
    super(message);
  }
}
