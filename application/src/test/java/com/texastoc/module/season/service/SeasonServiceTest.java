package com.texastoc.module.season.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.model.SystemSettings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SeasonServiceTest implements TestConstants {

  private SeasonService seasonService;

  private SeasonRepository seasonRepository;
  private GameModule gameModule;
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
    gameModule = mock(GameModule.class);
    ReflectionTestUtils.setField(seasonService, "gameModule", gameModule);
  }

  @Test
  public void createSeason() {
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
    Season actual = seasonService.create(start.getYear());

    // Assert
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

  @Test
  public void getSeason() {
    // Arrange
    Season season = Season.builder().id(1).build();
    when(seasonRepository.findById(1)).thenReturn(Optional.of(season));

    // Act
    Season actualSeason = seasonService.get(1);

    // Assert
    Mockito.verify(seasonRepository, Mockito.times(1)).findById(1);
    assertNotNull("season return from get should not be null ", actualSeason);
    assertEquals(1, actualSeason.getId());
  }

  @Test
  public void getSeasonNotFound() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.empty());

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.get(1);
    }).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Season with id 1 not found");
  }

  @Test
  public void getCurrentUnfinalized() {
    // Arrange
    Season unfinalziedSeason = Season.builder().id(1).build();
    when(seasonRepository.findUnfinalized()).thenReturn(Arrays.asList(unfinalziedSeason));

    // Act
    Season currentSeason = seasonService.getCurrent();
    // Assert
    Mockito.verify(seasonRepository, Mockito.times(1)).findUnfinalized();
    assertEquals(1, currentSeason.getId());

    // Act
    int currentSeasonId = seasonService.getCurrentId();
    assertEquals(1, currentSeasonId);

  }

  @Test
  public void getCurrentMostRecent() {
    // Arrange
    Season mostRecentSeason = Season.builder().id(2).build();
    when(seasonRepository.findUnfinalized()).thenReturn(Collections.emptyList());
    when(seasonRepository.findMostRecent()).thenReturn(Arrays.asList(mostRecentSeason));

    // Act
    Season currentSeason = seasonService.getCurrent();
    // Assert
    Mockito.verify(seasonRepository, Mockito.times(1)).findUnfinalized();
    assertEquals(2, currentSeason.getId());

    // Act
    int currentSeasonId = seasonService.getCurrentId();
    assertEquals(2, currentSeasonId);
  }

  @Test
  public void getCurrentNotFound() {
    // Arrange
    when(seasonRepository.findUnfinalized()).thenReturn(Collections.emptyList());
    when(seasonRepository.findMostRecent()).thenReturn(Collections.emptyList());

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.getCurrent();
    }).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Current season not found");

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.getCurrentId();
    }).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Current season not found");
  }

  @Test
  public void getAllSeasons() {
    // Arrange
    List<Season> noSeasons = Collections.emptyList();

    Season season1 = Season.builder().id(1).build();
    List<Season> oneSeason = Arrays.asList(season1);

    Season season2 = Season.builder().id(1).build();
    List<Season> twoSeasons = Arrays.asList(season1, season2);

    // Arrange
    when(seasonRepository.findAll()).thenReturn(noSeasons);
    // Act
    List<Season> actualSeasons = seasonService.getAll();
    // Assert
    Mockito.verify(seasonRepository, Mockito.times(1)).findAll();
    assertEquals(0, actualSeasons.size());

    // Arrange
    when(seasonRepository.findAll()).thenReturn(oneSeason);
    // Act
    actualSeasons = seasonService.getAll();
    // Assert
    Mockito.verify(seasonRepository, Mockito.times(2)).findAll();
    assertEquals(1, actualSeasons.size());

    // Arrange
    when(seasonRepository.findAll()).thenReturn(twoSeasons);
    // Act
    actualSeasons = seasonService.getAll();
    // Assert
    Mockito.verify(seasonRepository, Mockito.times(3)).findAll();
    assertEquals(2, actualSeasons.size());
  }

  @Test
  public void endSeason() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.of(Season.builder().id(1).build()));
    when(gameModule.getBySeasonId(1)).thenReturn(Collections.emptyList());

    // Act
    seasonService.end(1);

    // Assert
    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();
    assertTrue(season.isFinalized());
  }

  @Test
  public void cannotEndSeasonNotFound() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.empty());

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.end(1);
    }).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Season with id 1 not found");
  }

  @Test
  public void cannotEndSeasonGameInProgress() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.of(Season.builder().id(1).build()));
    when(gameModule.getBySeasonId(1)).thenReturn(Arrays.asList(Game.builder()
        .finalized(false)
        .build()));

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.end(1);
    }).isInstanceOf(GameInProgressException.class)
        .hasMessageContaining("There is a game in progress");
  }

  @Test
  public void openSeason() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.of(Season.builder()
        .id(1)
        .finalized(true)
        .build()));

    // Act
    seasonService.open(1);

    // Assert
    ArgumentCaptor<Season> seasonArg = ArgumentCaptor.forClass(Season.class);
    verify(seasonRepository, Mockito.times(1)).save(seasonArg.capture());
    Season season = seasonArg.getValue();
    assertFalse(season.isFinalized());
  }

  @Test
  public void cannotOpenSeasonNotFound() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.empty());

    // Act and Assert
    assertThatThrownBy(() -> {
      seasonService.open(1);
    }).isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Season with id 1 not found");
  }

  @Test
  public void openAlreadyOpenedSeason() {
    // Arrange
    when(seasonRepository.findById(1)).thenReturn(Optional.of(Season.builder()
        .id(1)
        .finalized(false)
        .build()));

    // Act
    seasonService.open(1);

    // Assert
    verify(seasonRepository, Mockito.times(0)).save(any());
  }

}
