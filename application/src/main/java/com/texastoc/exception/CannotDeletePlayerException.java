package com.texastoc.exception;

public class CannotDeletePlayerException extends RuntimeException {

  public CannotDeletePlayerException(String message) {
    super(message);
  }
}
