package com.texastoc.controller;

import com.texastoc.exception.NotFoundException;
import com.texastoc.model.system.Settings;
import com.texastoc.repository.SystemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

  private final SystemRepository systemRepository;

  public SystemController(SystemRepository systemRepository) {
    this.systemRepository = systemRepository;
  }

  @GetMapping("/api/v2/versions")
  public String getVersion(@RequestParam(required = false) String env) {
    if (env == null) {
      return "2.16";
    }
    Settings settings = systemRepository.get();
    for (Settings.Version version : settings.getUiVersions()) {
      if (env.equals(version.getEnv())) {
        return version.getVersion();
      }
    }
    throw new NotFoundException("environment " + env + " not found");
  }

}
