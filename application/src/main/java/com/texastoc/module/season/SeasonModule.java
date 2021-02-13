package com.texastoc.module.season;

import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import java.util.List;

public interface SeasonModule {

  Season create(int startYear);

  Season get(int id);

  Season getCurrent();

  int getCurrentId();

  void end(int seasonId);

  void open(int seasonId);

  List<HistoricalSeason> getPastSeasons();

}
