package com.texastoc.module.season.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.TestUtils;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SeasonServiceTest implements TestConstants {

  private SeasonService seasonService;

  private SeasonRepository seasonRepository;
  private SettingsModule settingsModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  @Before
  public void before() {
    seasonRepository = mock(SeasonRepository.class);
    seasonService = new SeasonService(seasonRepository);
    settingsModule = mock(SettingsModule.class);
    ReflectionTestUtils.setField(seasonService, "settingsModule", settingsModule);
    quarterlySeasonModule = mock(QuarterlySeasonModule.class);
    ReflectionTestUtils.setField(seasonService, "quarterlySeasonModule", quarterlySeasonModule);
  }

  @Test
  public void testCreateSeason() {

    // Arrange
    LocalDate now = LocalDate.now();
    LocalDate start = LocalDate.of(now.getYear(), Month.MAY, 1);

    Map<Integer, TocConfig> tocConfigMap = new HashMap<>();
    tocConfigMap.put(now.getYear(), TestConstants.getTocConfig());
    SystemSettings systemSettings = new SystemSettings();
    systemSettings.setTocConfigs(tocConfigMap);
    when(settingsModule.get()).thenReturn(systemSettings);

    LocalDate started = LocalDate.now();
    LocalDate ended = started.plusDays(1);
    Season savedSeason = Season.builder()
        .id(11)
        .start(started)
        .end(ended)
        .build();
    when(seasonRepository.save(any())).thenReturn(savedSeason);

    // Act
    Season actual = seasonService.createSeason(start.getYear());

    // Assert
    TestUtils.assertCreatedSeason(start, actual);

    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();

    assertEquals(start, seasonArg.getValue().getStart());

    assertEquals(KITTY_PER_GAME, season.getKittyPerGame());
    assertEquals(TOC_PER_GAME, season.getTocPerGame());
    assertEquals(QUARTERLY_TOC_PER_GAME, season.getQuarterlyTocPerGame());
    assertEquals(QUARTERLY_NUM_PAYOUTS, season.getQuarterlyNumPayouts());

    assertEquals(GAME_BUY_IN, season.getBuyInCost());
    assertEquals(GAME_REBUY, season.getRebuyAddOnCost());
    assertEquals(GAME_REBUY_TOC_DEBIT, season.getRebuyAddOnTocDebitCost());

    assertEquals(0, season.getBuyInCollected());
    assertEquals(0, season.getRebuyAddOnCollected());
    assertEquals(0, season.getAnnualTocCollected());
    assertEquals(0, season.getTotalCollected());

    assertEquals(0, season.getAnnualTocFromRebuyAddOnCalculated());
    assertEquals(0, season.getRebuyAddOnLessAnnualTocCalculated());
    assertEquals(0, season.getTotalCombinedAnnualTocCalculated());
    assertEquals(0, season.getKittyCalculated());
    assertEquals(0, season.getPrizePotCalculated());

    assertTrue(season.getNumGames() == 52 || season.getNumGames() == 53);
    assertEquals(0, season.getNumGamesPlayed());
    assertNull(season.getLastCalculated());
    assertFalse(season.isFinalized());

    assertTrue(season.getPlayers() == null || season.getPlayers().size() == 0);
    assertTrue(season.getPayouts() == null || season.getPayouts().size() == 0);
    assertTrue(season.getEstimatedPayouts() == null || season.getEstimatedPayouts().size() == 0);

    ArgumentCaptor<Integer> seasonIdArg = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<LocalDate> startArg = ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<LocalDate> endArg = ArgumentCaptor.forClass(LocalDate.class);
    verify(quarterlySeasonModule, times(1))
        .createQuarterlySeasons(seasonIdArg.capture(), startArg.capture(), endArg.capture());
    assertEquals(11, seasonIdArg.getValue().intValue());
    assertEquals(started, startArg.getValue());
    assertEquals(ended, endArg.getValue());
  }

//  @Test
//  public void testGetSeason() {
//
//    // Arrange
//    Season expectedSeason = Season.builder()
//      // @formatter:off
//      .id(1)
//      .build();
//    // @formatter:on
//
//    List<QuarterlySeason> qSeasons = new ArrayList<>(4);
//    for (int i = 1; i <= 4; i++) {
//      // @formatter:off
//      QuarterlySeason qSeason = QuarterlySeason.builder()
//        .id(i)
//        .quarter(Quarter.fromInt(i))
//        .build();
//      // @formatter:on
//      qSeasons.add(qSeason);
//    }
//
//    List<Game> games = new LinkedList<>();
//    // @formatter:off
//    Game game = Game.builder()
//      .id(1)
//      .build();
//    // @formatter:on
//    games.add(game);
//    expectedSeason.setGames(games);
//
//    Mockito.when(seasonRepository.get(1))
//      .thenReturn(Season.builder()
//        .id(1)
//        .build());
//
//    Mockito.when(qSeasonRepository.getBySeasonId(1)).thenReturn(qSeasons);
//
//    Mockito.when(gameRepository.getBySeasonId(1)).thenReturn(games);
//
//    // Act
//    Season actualSeason = service.getSeason(1);
//
//    // Season repository called once
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//
//    // QuarterlySeason repository called once
//    Mockito.verify(qSeasonRepository, Mockito.times(1)).getBySeasonId(1);
//
//    // Game repository called once
//    Mockito.verify(gameRepository, Mockito.times(1)).getBySeasonId(1);
//
//
//    // Assert
//    Assert.assertNotNull("season return from get should not be null ", actualSeason);
//    Assert.assertEquals(expectedSeason.getId(), actualSeason.getId());
//
//    Assert.assertNotNull("quarterly seasons should not be null ", actualSeason.getQuarterlySeasons());
//    Assert.assertEquals(4, actualSeason.getQuarterlySeasons().size());
//    for (int i = 1; i <= 4; i++) {
//      QuarterlySeason qSeason = actualSeason.getQuarterlySeasons().get(i - 1);
//      Assert.assertNotNull(qSeason);
//      Assert.assertTrue(qSeason.getId() > 0);
//    }
//
//    Assert.assertNotNull("season games should not be null ", actualSeason.getGames());
//    Assert.assertEquals(1, actualSeason.getGames().size());
//    Assert.assertTrue(actualSeason.getGames().get(0).getId() > 0);
//
//  }
//
//  // See TODO for caching in SeasonService
//  @Ignore
//  @Test
//  public void testCacheSeason() {
//    // If the cached season last calculated date is equal to the current
//    // season's last calcuated date then return the cached value
//    Season season1 = Season.builder()
//      .id(1)
//      .buyInCost(100)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season1);
//
//    Season season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(0)).getLastCalculated(1);
//
//    Assert.assertNull("last calculated should be null", season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 100", 100, season.getBuyInCost());
//
//    //
//    // The season should be cached
//    //
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(null);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(0)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertNull("last calculated should be null", season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 100", 100, season.getBuyInCost());
//
//    //
//    // Change the last calculated so that the cached value is not returned
//    //
//    LocalDateTime now = LocalDateTime.now();
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(now);
//
//    Season season2 = Season.builder()
//      .id(1)
//      .buyInCost(200)
//      .lastCalculated(now)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season2);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", now, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 200", 200, season.getBuyInCost());
//
//    //
//    // The season should be cached
//    //
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(now);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(0)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", now, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 200", 200, season.getBuyInCost());
//
//    //
//    // Change the last calculated so that the cached value is not returned
//    //
//    LocalDateTime later = LocalDateTime.now().plusSeconds(2l);
//    Mockito.reset(seasonRepository);
//    Mockito.when(seasonRepository.getLastCalculated(1)).thenReturn(later);
//
//    Season season3 = Season.builder()
//      .id(1)
//      .buyInCost(300)
//      .lastCalculated(later)
//      .build();
//
//    Mockito.when(seasonRepository.get(1)).thenReturn(season3);
//
//    season = service.getSeason(1);
//
//    Mockito.verify(seasonRepository, Mockito.times(1)).get(1);
//    Mockito.verify(seasonRepository, Mockito.times(1)).getLastCalculated(1);
//
//    Assert.assertEquals("last calculated should match", later, season.getLastCalculated());
//    Assert.assertEquals("buyInCost should be 300", 300, season.getBuyInCost());
//
//
//  }
}
