package com.texastoc.module.quarterly;

import com.texastoc.module.quarterly.model.QuarterlySeason;
import java.time.LocalDate;
import java.util.List;

public interface QuarterlySeasonModule {

  /**
   * Create the four quarterly seasons
   *
   * @param seasonId  the season Id
   * @param startYear the year the season starts
   */
  void create(int seasonId, int startYear);

  List<QuarterlySeason> getBySeasonId(int seasonId);

  QuarterlySeason getByDate(LocalDate date);
}
