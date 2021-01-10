package com.texastoc.module.settings.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.repository.SettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SettingsService implements SettingsModule {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final SettingsRepository settingsRepository;
  private final String payoutsFileName;
  private Map<Integer, List<Payout>> payouts;

  public SettingsService(SettingsRepository settingsRepository, @Value("${payouts.fileName}")
     String payoutsFileName) {
    this.settingsRepository = settingsRepository;
    this.payoutsFileName = payoutsFileName;

    try {
      payouts = OBJECT_MAPPER.readValue(getPayoutsAsJson(), new TypeReference<Map<Integer, List<Payout>>>() {
      });
    } catch (Exception e) {
      log.warn("Could not process payouts json", e);
      payouts = new HashMap<>();
    }
  }

  // TODO cache
  @Override
  public SystemSettings get() {
    Settings settings = settingsRepository.findById(1).get();
    return new SystemSettings(settings.getId(), settings.getVersion(), settings.getTocConfigs(), payouts);
  }

  private String getPayoutsAsJson() throws IOException {
    InputStream inputStream = new ClassPathResource(payoutsFileName).getInputStream();
    try (BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return bf.lines().collect(Collectors.joining());
    }
  }
}