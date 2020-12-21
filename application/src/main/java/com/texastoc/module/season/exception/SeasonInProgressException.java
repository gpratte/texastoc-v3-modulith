package com.texastoc.module.season.exception;

public class SeasonInProgressException extends RuntimeException {
  public SeasonInProgressException() {
    super("Current season is in progress");
  }
}
