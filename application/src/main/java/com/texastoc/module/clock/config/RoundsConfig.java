package com.texastoc.module.clock.config;

import com.texastoc.module.game.model.clock.Round;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "clock")
public class RoundsConfig {
  private List<Round> rounds;
}
