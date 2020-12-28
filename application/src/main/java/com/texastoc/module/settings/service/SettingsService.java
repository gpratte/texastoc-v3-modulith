package com.texastoc.module.settings.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.repository.SettingsRepository;
import lombok.extern.slf4j.Slf4j;
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
  private Map<Integer, List<Payout>> payouts;

  public SettingsService(SettingsRepository settingsRepository) {
    this.settingsRepository = settingsRepository;

    String json = getPayoutsAsJson();
    try {
      payouts = OBJECT_MAPPER.readValue(json, new TypeReference<Map<Integer, List<Payout>>>() {
      });
    } catch (JsonProcessingException e) {
      log.warn("Could not deserialize payouts json");
      payouts = new HashMap<>();
    }
  }

  // TODO cache
  @Override
  public SystemSettings get() {
    Settings settings = settingsRepository.findById(1).get();
    return new SystemSettings(settings.getId(), settings.getVersion(), settings.getTocConfigs(), payouts);
  }

  private String getPayoutsAsJson() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("payouts-percentages.json").getInputStream();
    } catch (IOException e) {
      return null;
    }
    try (BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return bf.lines().collect(Collectors.joining());
    } catch (IOException e) {
      return null;
    }
  }
}