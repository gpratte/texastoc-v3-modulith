package com.texastoc.controller;

import com.texastoc.model.season.HistoricalSeason;
import com.texastoc.model.season.Season;
import com.texastoc.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SeasonRestController {

  private final SeasonService seasonService;

  @Autowired
  public SeasonRestController(SeasonService seasonService) {
    this.seasonService = seasonService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/api/v2/seasons")
  public Season createSeason(@RequestBody SeasonStart seasonStart) {
    return seasonService.createSeason(seasonStart.getStartYear());
  }

  @GetMapping("/api/v2/seasons/{id}")
  public Season getSeason(@PathVariable("id") int id) {
    return seasonService.getSeason(id);
  }

  @GetMapping("/api/v2/seasons")
  public List<Season> getSeasons() {
    return seasonService.getSeasons();
  }

  @GetMapping("/api/v2/seasons/current")
  public Season getCurrentSeason() {
    int id = seasonService.getCurrentSeason().getId();
    return seasonService.getSeason(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v2/seasons/{id}", consumes = "application/vnd.texastoc.finalize+json")
  public void finalizeSeason(@PathVariable("id") int id) {
    seasonService.endSeason(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v2/seasons/{id}", consumes = "application/vnd.texastoc.unfinalize+json")
  public void unfinalizeSeason(@PathVariable("id") int id) {
    seasonService.openSeason(id);
  }

  @GetMapping("/api/v2/seasons/history")
  public List<HistoricalSeason> getPastSeasons() {
    return seasonService.getPastSeasons();
  }

  private static class SeasonStart {
    private int startYear;

    public int getStartYear() {
      return startYear;
    }

    public void setStartYear(int startYear) {
      this.startYear = startYear;
    }
  }
}
