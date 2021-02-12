package com.texastoc.module.season;

import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.service.HistoricalSeasonService;
import com.texastoc.module.season.service.SeasonService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeasonModuleImpl implements SeasonModule {

  private final SeasonService seasonService;
  private final HistoricalSeasonService historicalSeasonService;

  public SeasonModuleImpl(SeasonService seasonService,
      HistoricalSeasonService historicalSeasonService) {
    this.seasonService = seasonService;
    this.historicalSeasonService = historicalSeasonService;
  }

  @Override
  public Season create(int startYear) {
    return seasonService.create(startYear);
  }

  @Override
  public Season get(int id) {
    return seasonService.get(id);
  }

  @Override
  public Season getCurrent() {
    return seasonService.getCurrent();
  }

  @Override
  public int getCurrentId() {
    return seasonService.getCurrentId();
  }

  @Override
  public void end(int seasonId) {
    seasonService.end(seasonId);
  }

  @Override
  public void open(int seasonId) {
    seasonService.open(seasonId);
  }

  @Override
  public List<HistoricalSeason> getPastSeasons() {
    return historicalSeasonService.getPastSeasons();
  }
}
