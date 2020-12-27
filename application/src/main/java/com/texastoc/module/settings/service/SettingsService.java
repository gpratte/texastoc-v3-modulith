package com.texastoc.module.settings.service;

import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SettingsService implements SettingsModule {
  private final SettingsRepository settingsRepository;

  public SettingsService(SettingsRepository settingsRepository) {
    this.settingsRepository = settingsRepository;
  }

  // TODO cache
  @Override
  public Settings get() {
    List<Settings> settings = StreamSupport.stream(settingsRepository.findAll().spliterator(), false).collect(Collectors.toList());
    return settings.get(0);
  }
}
