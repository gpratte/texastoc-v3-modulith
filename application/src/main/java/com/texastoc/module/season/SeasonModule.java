package com.texastoc.module.season;

import com.texastoc.module.season.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;

import java.time.LocalDate;

public interface SeasonModule {
  int getCurrentSeasonId();
  Season getSeasonById(int id);
  Season getCurrentSeason();
  QuarterlySeason getQuarterlySeasonByDate(LocalDate date);
}
