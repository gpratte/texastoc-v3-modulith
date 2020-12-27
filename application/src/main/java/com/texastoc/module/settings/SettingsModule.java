package com.texastoc.module.settings;

import com.texastoc.module.settings.model.Settings;

public interface SettingsModule {
  /**
   * Get all settings
   * @return
   */
  Settings get();
}
