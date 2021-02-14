package com.texastoc.module.settings.model;

import java.util.List;
import java.util.Map;

public class SystemSettings extends Settings {

  private Map<Integer, List<Payout>> payouts;

  public SystemSettings() {
    super();
  }

  public SystemSettings(int id, Version version, Map<Integer, TocConfig> tocConfigs,
      Map<Integer, List<Payout>> payouts) {
    super(id, version, tocConfigs);
    this.payouts = payouts;
  }

  public Map<Integer, List<Payout>> getPayouts() {
    return payouts;
  }

  public void setPayouts(Map<Integer, List<Payout>> payouts) {
    this.payouts = payouts;
  }
}
