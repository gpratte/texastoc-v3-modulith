package com.texastoc.controller;

import com.texastoc.model.game.clock.Clock;
import com.texastoc.model.game.clock.Round;
import com.texastoc.service.ClockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClockRestController {

  private final ClockService clockService;

  public ClockRestController(ClockService clockService) {
    this.clockService = clockService;
  }

  @GetMapping("/api/v2/games/{id}/clock")
  public Clock getClock(@PathVariable("id") int id) {
    return clockService.get(id);
  }

  @PostMapping(value = "/api/v2/games/{id}/clock", consumes = "application/vnd.texastoc.clock-resume+json")
  public void resume(@PathVariable("id") int id) {
    clockService.resume(id);
  }

  @PostMapping(value = "/api/v2/games/{id}/clock", consumes = "application/vnd.texastoc.clock-pause+json")
  public void pause(@PathVariable("id") int id) {
    clockService.pause(id);
  }

  @PostMapping(value = "/api/v2/games/{id}/clock", consumes = "application/vnd.texastoc.clock-back+json")
  public void back(@PathVariable("id") int id) {
    clockService.back(id);
  }

  @PostMapping(value = "/api/v2/games/{id}/clock", consumes = "application/vnd.texastoc.clock-forward+json")
  public void forward(@PathVariable("id") int id) {
    clockService.forward(id);
  }

  @GetMapping("/api/v2/clock/rounds")
  public List<Round> getRounds() {
    return clockService.getRounds();
  }

}
