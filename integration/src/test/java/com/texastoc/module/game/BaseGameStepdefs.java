package com.texastoc.module.game;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.game.model.Game;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;

public class BaseGameStepdefs extends BaseIntegrationTest {

  protected Game gameToCreate;
  protected Game gameCreated;
  protected Game gameRetrieved;
  protected HttpClientErrorException exception;

  protected void before() {
    // Before each scenario
    gameToCreate = null;
    gameCreated = null;
    gameRetrieved = null;
    exception = null;
  }

  protected void after() {
    // After each scenario
    super.after();
  }

  protected void aSeasonExists() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    createSeason(token);
  }

  protected void theGameStartsNow() throws Exception {
    gameToCreate = Game.builder()
      .date(LocalDate.now())
      .hostId(1)
      .transportRequired(false)
      .build();
  }

  protected void theGameIsCreated() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameCreated = createGame(gameToCreate, token);
  }

  protected void getCurrentGame() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    gameRetrieved = getCurrentGame(token);
  }

}
