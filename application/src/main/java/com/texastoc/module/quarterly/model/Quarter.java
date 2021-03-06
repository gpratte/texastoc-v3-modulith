package com.texastoc.module.quarterly.model;

public enum Quarter {
  FIRST(1, "1st"), SECOND(2, "2nd"), THIRD(3, "3rd"), FOURTH(4, "4th");

  private final int value;
  private final String text;

  Quarter(int value, String text) {
    this.value = value;
    this.text = text;
  }

  public int getValue() {
    return value;
  }

  @SuppressWarnings("unused")
  public String getText() {
    return text;
  }

  public static Quarter fromInt(int value) {
    Quarter[] quarters = Quarter.values();
    for (Quarter quart : quarters) {
      if (quart.getValue() == value) {
        return quart;
      }
    }

    throw new IllegalArgumentException("Unknown quarterly value " + value);
  }
}
