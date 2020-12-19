package com.texastoc.service;

import com.texastoc.connector.EmailConnector;
import com.texastoc.exception.CannotDeletePlayerException;
import com.texastoc.exception.NotFoundException;
import com.texastoc.model.user.Player;
import com.texastoc.repository.GamePlayerRepository;
import com.texastoc.repository.PlayerRepository;
import com.texastoc.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final RoleRepository roleRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailConnector emailConnector;

  // Only one server so cache the forgot password codes here
  private Map<String, String> forgotPasswordCodes = new HashMap<>();

  public PlayerService(PlayerRepository playerRepository, RoleRepository roleRepository, GamePlayerRepository gamePlayerRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailConnector emailConnector) {
    this.playerRepository = playerRepository;
    this.roleRepository = roleRepository;
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
      .build();

    int id = playerRepository.save(playerToCreate);

    // Default to USER role
    roleRepository.save(id);

    player.setId(id);
    return player;
  }

  @Transactional
  public void update(Player player) {
    Player playerToUpdate = playerRepository.get(player.getId());
    playerToUpdate.setFirstName(player.getFirstName());
    playerToUpdate.setLastName(player.getLastName());
    playerToUpdate.setEmail(player.getEmail());
    playerToUpdate.setPhone(player.getPhone());

    if (player.getPassword() != null) {
      playerToUpdate.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
    }

    playerRepository.update(playerToUpdate);
  }

  @Transactional(readOnly = true)
  public List<Player> get() {
    return playerRepository.get();
  }

  @Transactional(readOnly = true)
  public Player get(int id) {
    Player player = playerRepository.get(id);
    player.setPassword(null);
    return player;
  }

  @Transactional(readOnly = true)
  public Player getByEmail(String email) {
    Player player = playerRepository.getByEmail(email);
    player.setPassword(null);
    return player;
  }

  @Transactional(readOnly = true)
  public void delete(int id) {
    int numGames = gamePlayerRepository.getNumGamesByPlayerId(id);
    if (numGames > 0) {
      throw new CannotDeletePlayerException("Player with ID " + id + " cannot be deleted");
    }
    playerRepository.deleteRoleById(id);
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

    Player playerToUpdate = playerRepository.getByEmail(email);
    playerToUpdate.setPassword(bCryptPasswordEncoder.encode(password));

    playerRepository.update(playerToUpdate);
  }
}
