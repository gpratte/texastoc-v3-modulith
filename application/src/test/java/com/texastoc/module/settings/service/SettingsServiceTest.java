package com.texastoc.module.settings.service;

import com.texastoc.module.settings.model.Payout;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import com.texastoc.module.settings.model.Version;
import com.texastoc.module.settings.repository.SettingsRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SettingsServiceTest {

  private SettingsService settingsService;
  private SettingsRepository settingsRepository;

  @Before
  public void before() {
    settingsRepository = mock(SettingsRepository.class);
    settingsService = new SettingsService(settingsRepository, "payouts-percentages.json");
  }

  @Test
  public void systemSettings() {
    // Arrange
    Settings settings = new Settings();

    settings.setVersion(Version.builder()
      .version("1.1")
      .build());

    Map<Integer, TocConfig> tocConfigMap = new HashMap<>();
    tocConfigMap.put(2020, TocConfig.builder()
      .id(123)
      .build());
    settings.setTocConfigs(tocConfigMap);

    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SystemSettings actual = settingsService.get();

    // Assert
    assertEquals(123, actual.getTocConfigs().get(2020).getId());
    assertEquals("1.1", actual.getVersion().getVersion());

    Payout secondPlaceOfTwo = actual.getPayouts().get(2).get(1);
    assertEquals(2, secondPlaceOfTwo.getPlace());
    assertEquals(0.35, secondPlaceOfTwo.getPercent(), 0.0);

    Payout thirdPlaceOfThree = actual.getPayouts().get(3).get(2);
    assertEquals(3, thirdPlaceOfThree.getPlace());
    assertEquals(0.2, thirdPlaceOfThree.getPercent(), 0.0);
  }

  @Test
  public void testJsonFileNotFound() {
    // Arrange
    Settings settings = new Settings();
    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SettingsService ss = new SettingsService(settingsRepository, "does-not-exist.json");
    SystemSettings actual = ss.get();

    // Assert
    assertEquals(0, actual.getPayouts().size());
  }

  @Test
  public void testBadJson() {
    // Arrange
    Settings settings = new Settings();
    when(settingsRepository.findById(1)).thenReturn(java.util.Optional.of(settings));

    // Act
    SettingsService ss = new SettingsService(settingsRepository, "bad-payout-percentages.json");
    SystemSettings actual = ss.get();

    // Assert
    assertEquals(0, actual.getPayouts().size());
  }

}
