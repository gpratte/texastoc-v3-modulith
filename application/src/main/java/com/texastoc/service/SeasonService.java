package com.texastoc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.DuplicateSeasonException;
import com.texastoc.exception.GameInProgressException;
import com.texastoc.exception.NotFoundException;
import com.texastoc.exception.SeasonInProgressException;
import com.texastoc.model.config.TocConfig;
import com.texastoc.model.game.Game;
import com.texastoc.model.season.HistoricalSeason;
import com.texastoc.model.season.Quarter;
import com.texastoc.model.season.QuarterlySeason;
import com.texastoc.model.season.Season;
import com.texastoc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeasonService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final SeasonRepository seasonRepository;
  private final QuarterlySeasonRepository qSeasonRepository;
  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final GamePayoutRepository gamePayoutRepository;
  private final SeasonPlayerRepository seasonPlayerRepository;
  private final ConfigRepository configRepository;
  private final SeasonPayoutRepository seasonPayoutRepository;
  private final SeasonHistoryRepository seasonHistoryRepository;
  private final QuarterlySeasonPlayerRepository qSeasonPlayerRepository;
  private final QuarterlySeasonPayoutRepository qSeasonPayoutRepository;

  private String pastSeasonsAsJson = null;

  @Autowired
  public SeasonService(SeasonRepository seasonRepository, QuarterlySeasonRepository qSeasonRepository, GameRepository gameRepository, ConfigRepository configRepository, GamePlayerRepository gamePlayerRepository, GamePayoutRepository gamePayoutRepository, SeasonPlayerRepository seasonPlayerRepository, SeasonPayoutRepository seasonPayoutRepository, SeasonHistoryRepository seasonHistoryRepository, QuarterlySeasonPlayerRepository qSeasonPlayerRepository, QuarterlySeasonPayoutRepository qSeasonPayoutRepository) {
    this.seasonRepository = seasonRepository;
    this.qSeasonRepository = qSeasonRepository;
    this.gameRepository = gameRepository;
    this.configRepository = configRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.gamePayoutRepository = gamePayoutRepository;
    this.seasonPlayerRepository = seasonPlayerRepository;
    this.seasonPayoutRepository = seasonPayoutRepository;
    this.seasonHistoryRepository = seasonHistoryRepository;
    this.qSeasonPlayerRepository = qSeasonPlayerRepository;
    this.qSeasonPayoutRepository = qSeasonPayoutRepository;
  }

  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
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

    TocConfig tocConfig = configRepository.get();

    // Count the number of Thursdays between the start and end inclusive
    int numThursdays = 0;
    LocalDate thursday = findNextThursday(start);
    while (thursday.isBefore(end) || thursday.isEqual(end)) {
      ++numThursdays;
      thursday = thursday.plusWeeks(1);
    }

    List<QuarterlySeason> qSeasons = createQuarterlySeasons(start, end, tocConfig);

    Season newSeason = Season.builder()
      .start(start)
      .end(end)
      .kittyPerGame(tocConfig.getKittyDebit())
      .tocPerGame(tocConfig.getAnnualTocCost())
      .quarterlyTocPerGame(tocConfig.getQuarterlyTocCost())
      .quarterlyNumPayouts(tocConfig.getQuarterlyNumPayouts())
      .buyInCost(tocConfig.getRegularBuyInCost())
      .rebuyAddOnCost(tocConfig.getRegularRebuyCost())
      .rebuyAddOnTocDebit(tocConfig.getRegularRebuyTocDebit())
      .numGames(numThursdays)
      .quarterlySeasons(qSeasons)
      .build();

    int seasonId = seasonRepository.save(newSeason);
    newSeason.setId(seasonId);

    for (QuarterlySeason qSeason : qSeasons) {
      qSeason.setSeasonId(seasonId);
      int qSeasonId = qSeasonRepository.save(qSeason);
      qSeason.setId(qSeasonId);
    }

    return newSeason;
  }

  @Cacheable("currentSeasonById")
  @Transactional(readOnly = true)
  public Season getSeason(int id) {
    Season season = seasonRepository.get(id);
    season.setPlayers(seasonPlayerRepository.getBySeasonId(id));
    season.setPayouts(seasonPayoutRepository.getBySeasonId(id));
    season.setEstimatedPayouts(seasonPayoutRepository.getEstimatedBySeasonId(id));

    season.setQuarterlySeasons(qSeasonRepository.getBySeasonId(id));
    season.setGames(gameRepository.getBySeasonId(id));

    for (QuarterlySeason qSeason : season.getQuarterlySeasons()) {
      qSeason.setPlayers(qSeasonPlayerRepository.getByQSeasonId(qSeason.getId()));
      qSeason.setPayouts(qSeasonPayoutRepository.getByQSeasonId(qSeason.getId()));
    }

    for (Game game : season.getGames()) {
      game.setPlayers(gamePlayerRepository.selectByGameId(game.getId()));
      game.setPayouts(gamePayoutRepository.getByGameId(game.getId()));
    }

    return season;
  }

  @Transactional(readOnly = true)
  public List<Season> getSeasons() {
    return seasonRepository.getAll();
  }

  @Cacheable("currentSeason")
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

  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void endSeason(int seasonId) {
    Season season = seasonRepository.get(seasonId);
    // Make sure no games are open
    List<Game> games = gameRepository.getBySeasonId(seasonId);
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
      .forEach(seasonPlayer -> seasonHistoryRepository.savePlayer(seasonId, seasonPlayer.getName(), seasonPlayer.getPoints(), seasonPlayer.getEntries()));
  }

  @CacheEvict(value = {"currentSeason", "currentSeasonById"}, allEntries = true, beforeInvocation = false)
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
      historicalSeasonsFromJson = OBJECT_MAPPER.readValue(json, new TypeReference<List<HistoricalSeason>>() {
      });
    } catch (JsonProcessingException e) {
      log.warn("Could not deserialize historical seasons json");
      historicalSeasonsFromJson = new LinkedList<>();
    }

    List<HistoricalSeason> historicalSeasons = seasonHistoryRepository.getAll();
    historicalSeasons.forEach(historicalSeason -> historicalSeason.setPlayers(seasonHistoryRepository.getAllPlayers(historicalSeason.getSeasonId())));

    historicalSeasons.addAll(historicalSeasonsFromJson);
    return historicalSeasons;
  }


  private List<QuarterlySeason> createQuarterlySeasons(LocalDate seasonStart, LocalDate seasonEnd, TocConfig tocConfig) {
    List<QuarterlySeason> qSeasons = new ArrayList<>(4);
    for (int i = 1; i <= 4; ++i) {
      LocalDate qStart = null;
      LocalDate qEnd = null;
      switch (i) {
        case 1:
          // Season start
          qStart = seasonStart;
          // Last day in July
          qEnd = LocalDate.of(seasonStart.getYear(), Month.AUGUST.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 2:
          // First day in August
          qStart = LocalDate.of(seasonStart.getYear(), Month.AUGUST.getValue(), 1);
          // Last day in October
          qEnd = LocalDate.of(seasonStart.getYear(), Month.NOVEMBER.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 3:
          // First day in November
          qStart = LocalDate.of(seasonStart.getYear(), Month.NOVEMBER.getValue(), 1);
          // Last day in January
          qEnd = LocalDate.of(seasonStart.getYear() + 1, Month.FEBRUARY.getValue(), 1);
          qEnd = qEnd.minusDays(1);
          break;
        case 4:
          // First day in February
          qStart = LocalDate.of(seasonStart.getYear() + 1, Month.FEBRUARY.getValue(), 1);
          // End of season
          qEnd = seasonEnd;
          break;
      }

      // Count the number of Thursdays between the start and end inclusive
      int qNumThursdays = 0;
      LocalDate thursday = findNextThursday(qStart);
      while (thursday.isBefore(qEnd) || thursday.isEqual(qEnd)) {
        ++qNumThursdays;
        thursday = thursday.plusWeeks(1);
      }

      QuarterlySeason qSeason = QuarterlySeason.builder()
        .quarter(Quarter.fromInt(i))
        .start(qStart)
        .end(qEnd)
        .finalized(false)
        .numGames(qNumThursdays)
        .numGamesPlayed(0)
        .qTocCollected(0)
        .qTocPerGame(tocConfig.getQuarterlyTocCost())
        .numPayouts(tocConfig.getQuarterlyNumPayouts())
        .build();
      qSeasons.add(qSeason);
    }
    return qSeasons;
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
    try (BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      pastSeasonsAsJson = bf.lines().collect(Collectors.joining());
      return pastSeasonsAsJson;
    } catch (IOException e) {
      return null;
    }
  }
}
