package com.texastoc.module.settings.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

  @Id
  private int id;
  @MappedCollection(idColumn = "ID")
  Version version;
  @MappedCollection
  private Map<Integer, TocConfig> tocConfigs;
}
