package com.texastoc.module.season.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.season.exception.DuplicateSeasonException;
import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.exception.SeasonInProgressException;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.repository.SeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SeasonService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonRepository seasonRepository;

  private GameModule gameModule;
  private SettingsModule settingsModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  @Autowired
  public SeasonService(SeasonRepository seasonRepository) {
    this.seasonRepository = seasonRepository;
  }

  public int getCurrentSeasonId() {
    return getCurrent().getId();
  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public Season createSeason(int startYear) {
    LocalDate start = LocalDate.of(startYear, Month.MAY.getValue(), 1);
    try {
      Season currentSeason = getCurrent();
      if (!currentSeason.isFinalized()) {
        throw new SeasonInProgressException();
      }
    } catch (NotFoundException e) {
      // do nothing
    }

    // Make sure not overlapping with another season
    List<Season> seasons = getAll();
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
    getQuarterlySeasonModule()
        .createQuarterlySeasons(season.getId(), season.getStart(), season.getEnd());

    return season;
  }

  //  @Cacheable("currentSeasonById")
  @Transactional(readOnly = true)
  public Season get(int id) {
    Optional<Season> optionalSeason = seasonRepository.findById(id);
    if (!optionalSeason.isPresent()) {
      throw new NotFoundException("Season with id " + id + " not found");
    }
    return optionalSeason.get();

  }

  @Transactional(readOnly = true)
  public List<Season> getAll() {
    return StreamSupport.stream(seasonRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  //  @Cacheable("currentSeason")
  @Transactional(readOnly = true)
  public Season getCurrent() {
    Season season = null;
    List<Season> seasons = seasonRepository.findUnfinalized();
    if (seasons.size() > 0) {
      season = seasons.get(0);
    } else {
      seasons = seasonRepository.findMostRecent();
      if (seasons.size() > 0) {
        season = seasons.get(0);
      }
    }

    if (season == null) {
      throw new NotFoundException("Could not find current season");
    }

    return season;
  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void endSeason(int seasonId) {
    Season season = get(seasonId);
    // Make sure no games are open
    List<Game> games = getGameModule().getBySeasonId(seasonId);
    for (Game game : games) {
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress.");
      }
    }

    season.setFinalized(true);
    seasonRepository.save(season);

    // TODO
    // Clear out the historical season
//    seasonHistoryRepository.deletePlayersById(seasonId);
//    seasonHistoryRepository.deleteById(seasonId);
    // Set the historical season
//    seasonHistoryRepository.save(seasonId, season.getStart(), season.getEnd());
//    season.getPlayers()
//        .forEach(seasonPlayer -> seasonHistoryRepository
//            .savePlayer(seasonId, seasonPlayer.getName(), seasonPlayer.getPoints(),
//                seasonPlayer.getEntries()));
  }

  //  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void openSeason(int seasonId) {
    Season season = get(seasonId);
    season.setFinalized(false);
    seasonRepository.save(season);

    // TODO
    // Clear out the historical season
    //seasonHistoryRepository.deletePlayersById(seasonId);
    //seasonHistoryRepository.deleteById(seasonId);
  }

  private LocalDate findNextThursday(LocalDate day) {
    while (true) {
      if (day.getDayOfWeek() == DayOfWeek.THURSDAY) {
        return day;
      }
      day = day.plusDays(1);
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
