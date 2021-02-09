package com.texastoc.module.season.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.exception.DuplicateSeasonException;
import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.exception.SeasonInProgressException;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.repository.SeasonHistoryRepository;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SeasonService implements SeasonModule {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonRepository seasonRepository;
  private final SeasonHistoryRepository seasonHistoryRepository;

  private GameModule gameModule;
  private SettingsModule settingsModule;
  private QuarterlySeasonModule quarterlySeasonModule;
  private String pastSeasonsAsJson = null;

  @Autowired
  public SeasonService(SeasonRepository seasonRepository,
      SeasonHistoryRepository seasonHistoryRepository) {
    this.seasonRepository = seasonRepository;
    this.seasonHistoryRepository = seasonHistoryRepository;
  }

  @Override
  public int getCurrentSeasonId() {
    return getCurrentSeason().getId();
  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Season createSeason(int startYear) {
    LocalDate start = LocalDate.of(startYear, Month.MAY.getValue(), 1);
    try {
      Season currentSeason = getCurrentSeason();
      if (!currentSeason.isFinalized()) {
        throw new SeasonInProgressException();
      }
    } catch (NotFoundException e) {
      // do nothing
    }

    // Make sure not overlapping with another season
    List<Season> seasons = getSeasons();
    seasons.forEach(season -> {
      if (season.getStart().getYear() == startYear) {
        throw new DuplicateSeasonException(startYear);
      }
    });

    // The end will be the day before the start date next year
    LocalDate end = start.plusYears(1).minusDays(1);

    Settings settings = getSettingsModule().get();
    TocConfig tocConfig = settings.getTocConfigs().get(startYear);

    // Count the number of Thursdays between the start and end inclusive
    int numThursdays = 0;
    LocalDate thursday = findNextThursday(start);
    while (thursday.isBefore(end) || thursday.isEqual(end)) {
      ++numThursdays;
      thursday = thursday.plusWeeks(1);
    }

    Season newSeason = Season.builder()
        .start(start)
        .end(end)
        .kittyPerGame(tocConfig.getKittyDebit())
        .tocPerGame(tocConfig.getAnnualTocCost())
        .quarterlyTocPerGame(tocConfig.getQuarterlyTocCost())
        .quarterlyNumPayouts(tocConfig.getQuarterlyNumPayouts())
        .buyInCost(tocConfig.getRegularBuyInCost())
        .rebuyAddOnCost(tocConfig.getRegularRebuyCost())
        .rebuyAddOnTocDebitCost(tocConfig.getRegularRebuyTocDebit())
        .numGames(numThursdays)
        .build();

    Season season = seasonRepository.save(newSeason);

    // TODO message instead
    getQuarterlySeasonModule().createQuarterlySeasons(season.getId(), start, end);

    return season;
  }

  //  @Cacheable("currentSeasonById")
  @Override
  @Transactional(readOnly = true)
  public Season getSeason(int id) {
    Optional<Season> optionalSeason = seasonRepository.findById(id);
    if (!optionalSeason.isPresent()) {
      throw new NotFoundException("Season with id " + id + " not found");
    }
    return optionalSeason.get();

  }

  @Transactional(readOnly = true)
  public List<Season> getSeasons() {
    return StreamSupport.stream(seasonRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
//  @Cacheable("currentSeason")
  @Transactional(readOnly = true)
  public Season getCurrentSeason() {
    Season season = null;
    List<Season> seasons = seasonRepository.getUnfinalized();
    if (seasons.size() > 0) {
      season = seasons.get(0);
    } else {
      seasons = seasonRepository.getMostRecent();
      if (seasons.size() > 0) {
        season = seasons.get(0);
      }
    }

    if (season == null) {
      throw new NotFoundException("Could not find current season");
    }

    return season;
  }

//  @Transactional(readOnly = true)
//  public int getCurrentSeasonId() {
//    return seasonRepository.getCurrent().getId();
//  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void endSeason(int seasonId) {
    Season season = seasonRepository.get(seasonId);
    // Make sure no games are open
    List<Game> games = getGameModule().getBySeasonId(seasonId);
    for (Game game : games) {
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    season.setFinalized(true);
    seasonRepository.update(season);

    // Clear out the historical season
    seasonHistoryRepository.deletePlayersById(seasonId);
    seasonHistoryRepository.deleteById(seasonId);

    // Set the historical season
    seasonHistoryRepository.save(seasonId, season.getStart(), season.getEnd());
    seasonPlayerRepository.getBySeasonId(seasonId)
        .forEach(seasonPlayer -> seasonHistoryRepository
            .savePlayer(seasonId, seasonPlayer.getName(), seasonPlayer.getPoints(),
                seasonPlayer.getEntries()));
  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void openSeason(int seasonId) {
    Season season = seasonRepository.get(seasonId);
    season.setFinalized(false);
    seasonRepository.update(season);

    // Clear out the historical season
    seasonHistoryRepository.deletePlayersById(seasonId);
    seasonHistoryRepository.deleteById(seasonId);
  }

  public List<HistoricalSeason> getPastSeasons() {
    List<HistoricalSeason> historicalSeasonsFromJson = null;
    String json = getPastSeasonsAsJson();
    try {
      historicalSeasonsFromJson = OBJECT_MAPPER
          .readValue(json, new TypeReference<List<HistoricalSeason>>() {
          });
    } catch (JsonProcessingException e) {
      log.warn("Could not deserialize historical seasons json");
      historicalSeasonsFromJson = new LinkedList<>();
    }

    List<HistoricalSeason> historicalSeasons = seasonHistoryRepository.getAll();
    historicalSeasons.forEach(historicalSeason -> historicalSeason
        .setPlayers(seasonHistoryRepository.getAllPlayers(historicalSeason.getSeasonId())));

    historicalSeasons.addAll(historicalSeasonsFromJson);
    return historicalSeasons;
  }


  private LocalDate findNextThursday(LocalDate day) {
    while (true) {
      if (day.getDayOfWeek() == DayOfWeek.THURSDAY) {
        return day;
      }
      day = day.plusDays(1);
    }
  }

  private String getPastSeasonsAsJson() {
    if (pastSeasonsAsJson != null) {
      return pastSeasonsAsJson;
    }
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("season_history.json").getInputStream();
    } catch (IOException e) {
      return null;
    }
    try (BufferedReader bf = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      pastSeasonsAsJson = bf.lines().collect(Collectors.joining());
      return pastSeasonsAsJson;
    } catch (IOException e) {
      return null;
    }
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

  private QuarterlySeasonModule getQuarterlySeasonModule() {
    if (quarterlySeasonModule == null) {
      quarterlySeasonModule = QuarterlySeasonModuleFactory.getQuarterlySeasonModule();
    }
    return quarterlySeasonModule;
  }
}
