package com.texastoc.controller;

import com.texastoc.service.LeagueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LeagueController {

  private final LeagueService leagueService;

  public LeagueController(LeagueService leagueService) {
    this.leagueService = leagueService;
  }

  @GetMapping("/api/v2/league/points")
  public Map<Integer, Map<Integer, Integer>> getVersions() {
    return leagueService.getPoints();
  }
}
