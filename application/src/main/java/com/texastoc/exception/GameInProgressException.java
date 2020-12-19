package com.texastoc.exception;

public class GameInProgressException extends RuntimeException {

  public GameInProgressException(String message) {
    super(message);
  }
}
