package com.texastoc.module.player;

import com.google.common.collect.ImmutableSet;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.repository.GamePlayerRepository;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.exception.CannotDeletePlayerException;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailConnector emailConnector;

  // Only one server so cache the forgot password codes here
  private Map<String, String> forgotPasswordCodes = new HashMap<>();

  public PlayerService(PlayerRepository playerRepository, GamePlayerRepository gamePlayerRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailConnector emailConnector) {
    this.playerRepository = playerRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.emailConnector = emailConnector;
  }

  @Transactional
  public Player create(Player player) {
    Player playerToCreate = Player.builder()
      .firstName(player.getFirstName())
      .lastName(player.getLastName())
      .email(player.getEmail())
      .phone(player.getPhone())
      .password(player.getPassword() == null ? null : bCryptPasswordEncoder.encode(player.getPassword()))
      .roles(ImmutableSet.of(Role.builder()
        .name(PlayerRepository.USER)
        .build()))
      .build();

    int id = playerRepository.save(playerToCreate).getId();

    player.setId(id);
    return player;
  }

  @Transactional
  public void update(Player player) {
    Player playerToUpdate = playerRepository.findById(player.getId()).get();
    playerToUpdate.setFirstName(player.getFirstName());
    playerToUpdate.setLastName(player.getLastName());
    playerToUpdate.setEmail(player.getEmail());
    playerToUpdate.setPhone(player.getPhone());

    if (player.getPassword() != null) {
      playerToUpdate.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
    }

    playerRepository.save(playerToUpdate);
  }

  @Transactional(readOnly = true)
  public List<Player> get() {
    return StreamSupport.stream(playerRepository.findAll().spliterator(), false)
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Player get(int id) {
    Player player = playerRepository.findById(id).get();
    player.setPassword(null);
    return player;
  }

  @Transactional(readOnly = true)
  public Player getByEmail(String email) {
    List<Player> players = playerRepository.findByEmail(email);
    if (players.size() != 1) {
      throw new NotFoundException("Could not find player with email " + email);
    }
    Player player = players.get(0);
    player.setPassword(null);
    return player;
  }

  @Transactional(readOnly = true)
  public void delete(int id) {
    int numGames = gamePlayerRepository.getNumGamesByPlayerId(id);
    if (numGames > 0) {
      throw new CannotDeletePlayerException("Player with ID " + id + " cannot be deleted");
    }
    playerRepository.deleteById(id);
  }

  public void sendCode(String email) {
    String generatedString = RandomStringUtils.random(5, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
    forgotPasswordCodes.put(email, generatedString);
    log.info("reset code: {}", generatedString);
    emailConnector.send(email, "Reset Code", generatedString);
  }

  public void resetPassword(String code, String password) {
    String email = null;
    for (Map.Entry<String, String> forgotCode : forgotPasswordCodes.entrySet()) {
      if (forgotCode.getValue().equals(code)) {
        email = forgotCode.getKey();
        break;
      }
    }

    if (email == null) {
      throw new NotFoundException("No code found");
    }

    forgotPasswordCodes.remove(email);

    Player playerToUpdate = playerRepository.findByEmail(email).get(0);
    playerToUpdate.setPassword(bCryptPasswordEncoder.encode(password));

    playerRepository.save(playerToUpdate);
  }
}
