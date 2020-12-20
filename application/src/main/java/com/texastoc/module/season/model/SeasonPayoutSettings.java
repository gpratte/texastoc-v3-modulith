package com.texastoc.module.season.model;

import lombok.Data;

import java.util.List;

@Data
public class SeasonPayoutSettings {

  private int id;
  private int seasonId;
  private List<SeasonPayoutRange> ranges;
}
