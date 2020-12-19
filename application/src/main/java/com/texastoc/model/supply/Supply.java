package com.texastoc.model.supply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supply {
  private int id;
  private LocalDate date;
  private SupplyType type;
  private int amount;
  private String description;
}
