package com.texastoc.module.settings.service;

import com.texastoc.module.settings.model.*;
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
    settingsService = new SettingsService(settingsRepository);
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
}
