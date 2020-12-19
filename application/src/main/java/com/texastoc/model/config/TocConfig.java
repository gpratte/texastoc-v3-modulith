package com.texastoc.model.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TocConfig {

  private int kittyDebit;
  private int annualTocCost;
  private int quarterlyTocCost;
  private int quarterlyNumPayouts;
  private int regularBuyInCost;
  private int regularRebuyCost;
  private int regularRebuyTocDebit;
}
