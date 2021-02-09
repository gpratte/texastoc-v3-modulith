package com.texastoc.module.season;

import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;

import java.util.List;

public interface SeasonModule {
  
  Season createSeason(int startYear);

  int getCurrentSeasonId();

  Season getSeason(int id);

  Season getCurrentSeason();

  void endSeason(int seasonId);

  void openSeason(int seasonId);

  List<HistoricalSeason> getPastSeasons();

}
