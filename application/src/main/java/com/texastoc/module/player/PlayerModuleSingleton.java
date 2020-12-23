package com.texastoc.module.player;

import com.texastoc.module.player.service.PlayerService;
import org.springframework.stereotype.Component;

@Component
public class PlayerModuleSingleton {

  private static PlayerModule PLAYER_MODULE;

  public PlayerModuleSingleton(PlayerService playerService) {
    PLAYER_MODULE = playerService;
  }

  public static PlayerModule getPlayerModule() {
    return PLAYER_MODULE;
  }
}
