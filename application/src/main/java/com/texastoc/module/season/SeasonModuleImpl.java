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
  public Season createSeason(int startYear) {
    return seasonService.createSeason(startYear);
  }

  @Override
  public int getCurrentSeasonId() {
    return seasonService.getCurrentSeasonId();
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
  public void endSeason(int seasonId) {
    seasonService.endSeason(seasonId);
  }

  @Override
  public void openSeason(int seasonId) {
    seasonService.openSeason(seasonId);
  }

  @Override
  public List<HistoricalSeason> getPastSeasons() {
    return historicalSeasonService.getPastSeasons();
  }
}
