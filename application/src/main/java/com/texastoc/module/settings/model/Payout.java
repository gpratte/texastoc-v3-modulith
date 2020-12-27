package com.texastoc.module.settings.model;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Payout {
  private int place;
  private double percent;
}
