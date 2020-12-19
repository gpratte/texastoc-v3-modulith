package com.texastoc.model.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
  private List<Version> uiVersions;

  @Data
  @NoArgsConstructor
  public static class Version {
    private String env;
    private String version;
  }

}
