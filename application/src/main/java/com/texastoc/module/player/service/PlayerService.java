package com.texastoc.module.player.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class PlayerService implements PlayerModule {

  private final PlayerRepository playerRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailConnector emailConnector;

  // Only one server so cache the forgot password codes here
  private Map<String, String> forgotPasswordCodes = new HashMap<>();

  public PlayerService(PlayerRepository playerRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailConnector emailConnector) {
    this.playerRepository = playerRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.emailConnector = emailConnector;
  }

  @Override
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

  @Override
  @Transactional
  public void update(Player player) {
    Player existingPlayer = playerRepository.findById(player.getId()).get();
    player.setPassword(existingPlayer.getPassword());
    player.setRoles((existingPlayer.getRoles()));
    playerRepository.save(player);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Player> getAll() {
    List<Player> players = StreamSupport.stream(playerRepository.findAll().spliterator(), false).collect(Collectors.toList());
    Collections.sort(players);
    return players;
  }

  @Override
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

  @Override
  @Transactional
  public void delete(int id) {
    // TODO call game service to see if player has any games
//    if (player has any games) {
//      throw new CannotDeletePlayerException("Player with ID " + id + " cannot be deleted");
//    }
    playerRepository.deleteById(id);
  }

  @Override
  public void forgotPassword(String email) {
    String generatedString = RandomStringUtils.random(5, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
    forgotPasswordCodes.put(email, generatedString);
    log.info("reset code: {}", generatedString);
    emailConnector.send(email, "Reset Code", generatedString);
  }

  @Override
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
