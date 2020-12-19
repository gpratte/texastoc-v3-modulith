package com.texastoc.service;

import com.texastoc.service.calculator.PointsCalculator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LeagueService {

  private final PointsCalculator pointsCalculator;

  public LeagueService(PointsCalculator pointsCalculator) {
    this.pointsCalculator = pointsCalculator;
  }

  @Cacheable("points")
  public Map<Integer, Map<Integer, Integer>> getPoints() {
    Map<Integer, Map<Integer, Integer>> pointSystem = new HashMap<>();
    for (int i = 2; i <= 50; i++) {
      pointSystem.put(i, pointsCalculator.calculatePlacePoints(i));
    }
    return pointSystem;
  }
}
