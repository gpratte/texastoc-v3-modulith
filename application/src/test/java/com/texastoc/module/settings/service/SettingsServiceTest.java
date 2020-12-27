package com.texastoc.module.settings.service;

import com.texastoc.module.settings.model.*;
import com.texastoc.module.settings.repository.SettingsRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    ReflectionTestUtils.setField(settingsService, "payouts", generatePayouts());
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

  private Object generatePayouts() {
    Map<Integer, List<Payout>> payouts = new HashMap<>();

    // 2
    Payout payout = new Payout();
    payout.setPlace(1);
    payout.setPercent(0.65);
    List<Payout> list = new LinkedList<>();
    list.add(payout);
    payout = new Payout();
    payout.setPlace(2);
    payout.setPercent(0.35);
    list.add(payout);
    payouts.put(2, list);

    // 3
    payout = new Payout();
    payout.setPlace(1);
    payout.setPercent(0.5);
    list = new LinkedList<>();
    list.add(payout);
    payout = new Payout();
    payout.setPlace(2);
    payout.setPercent(0.3);
    list.add(payout);
    payout = new Payout();
    payout.setPlace(3);
    payout.setPercent(0.2);
    list.add(payout);
    payouts.put(3, list);

    return payouts;
  }
}
