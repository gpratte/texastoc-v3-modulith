package com.texastoc.module.settings.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Payout {
  @Id
  private int id;
  private int numPayouts;
  private int place;
  private double percent;
}
