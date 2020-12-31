package com.texastoc.module.game.service;

import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.notification.connector.EmailConnector;
import org.junit.Before;

import static org.mockito.Mockito.mock;

public class GameHelperTest {

  private GameHelper gameHelper;
  private GameRepository gameRepository;
  private GameCalculator gameCalculator;
  private PayoutCalculator payoutCalculator;
  private PointsCalculator pointsCalculator;
  private EmailConnector emailConnector = mock(EmailConnector.class);
  private WebSocketConnector webSocketConnector = mock(WebSocketConnector.class);

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameCalculator = mock(GameCalculator.class);
    payoutCalculator = mock(PayoutCalculator.class);
    gameHelper = new GameHelper(gameRepository, gameCalculator, payoutCalculator, pointsCalculator, emailConnector, webSocketConnector);
  }
}
