package com.texastoc.model.common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Payout {
  private int numPayouts;
  private int place;
  private double percent;
}
