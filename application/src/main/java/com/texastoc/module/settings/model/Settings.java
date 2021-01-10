package com.texastoc.module.settings.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
  @MappedCollection(idColumn = "ID", keyColumn = "YEAR")
  private Map<Integer, TocConfig> tocConfigs;
}
