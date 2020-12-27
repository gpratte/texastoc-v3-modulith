package com.texastoc.module.settings.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

  @Id
  private int id;
  @MappedCollection
  private List<TocConfig> tocConfigs;
  @MappedCollection
  private List<Version> versions;
  @MappedCollection
  private List<Payout> payouts;
}
