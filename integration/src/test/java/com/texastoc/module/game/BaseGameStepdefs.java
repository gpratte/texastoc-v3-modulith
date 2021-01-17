package com.texastoc.module.game;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.game.model.Game;
import org.springframework.web.client.HttpClientErrorException;

public class BaseGameStepdefs extends BaseIntegrationTest {

  //  private CreateGameRequest createGameRequest;
  protected Game gameToCreate;
  protected Game gameCreated;
  protected Game gameRetrieved;
  protected HttpClientErrorException exception;

  public void before() {
    // Before each scenario
    gameToCreate = null;
    gameCreated = null;
    gameRetrieved = null;
    exception = null;
  }

  public void after() {
    // After each scenario
    super.after();
  }

}
