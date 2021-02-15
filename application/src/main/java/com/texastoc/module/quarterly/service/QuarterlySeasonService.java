package com.texastoc.module.quarterly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.model.Quarter;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.repository.QuarterlySeasonRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuarterlySeasonService implements QuarterlySeasonModule {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final QuarterlySeasonRepository qSeasonRepository;

  private SettingsModule settingsModule;

  @Autowired
  public QuarterlySeasonService(QuarterlySeasonRepository qSeasonRepository) {
    this.qSeasonRepository = qSeasonRepository;
  }

  @Override
  public List<QuarterlySeason> getQuarterlySeasonBySeason(int seasonId) {
    return null;
  }

  @Override
  public QuarterlySeason getQuarterlySeasonByDate(LocalDate date) {
    List<QuarterlySeason> qSeasons = qSeasonRepository.findByDate(date);
    if (qSeasons.size() > 0) {
      return qSeasons.get(0);
    }
    throw new NotFoundException("Could not find a quarterly for date " + date);
  }

  @Override
  public void createQuarterlySeasons(int seasonId, LocalDate seasonStart, LocalDate seasonEnd) {
    Settings settings = getSettingsModule().get();
    TocConfig tocConfig = settings.getTocConfigs().get(seasonStart.getYear());

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

    // TODO persist quarterly seasons
  }

  private LocalDate findNextThursday(LocalDate day) {
    while (true) {
      if (day.getDayOfWeek() == DayOfWeek.THURSDAY) {
        return day;
      }
      day = day.plusDays(1);
    }
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

}
