package com.texastoc.connector;

import com.texastoc.model.game.Game;
import com.texastoc.model.game.clock.Clock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnector {

  private final SimpMessagingTemplate template;

  public WebSocketConnector(SimpMessagingTemplate template) {
    this.template = template;
  }

  /**
   * Send the clock on the websocket
   */
  public void sendClock(Clock clock) {
    template.convertAndSend("/topic/clock", clock);
  }

  /**
   * Send the game on the websocket
   */
  public void sendGame(Game game) {
    // Can send garbage until the client really needs the game
    template.convertAndSend("/topic/game", "garbage");
  }

}
