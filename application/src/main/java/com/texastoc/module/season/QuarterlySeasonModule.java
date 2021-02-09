package com.texastoc.module.season;

import com.texastoc.module.season.model.QuarterlySeason;
import java.time.LocalDate;
import java.util.List;

public interface QuarterlySeasonModule {

  void createQuarterlySeasons(int seasonId, LocalDate start, LocalDate end);

  List<QuarterlySeason> getQuarterlySeasonBySeason(int seasonId);

  QuarterlySeason getQuarterlySeasonByDate(LocalDate date);
}
