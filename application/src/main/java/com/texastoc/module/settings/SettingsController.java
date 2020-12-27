package com.texastoc.module.settings;

import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettingsController implements SettingsModule {

  private final SettingsService settingsService;

  public SettingsController(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Override
  @GetMapping("/api/v2/settings")
  public Settings get() {
    return settingsService.get();
  }

}
