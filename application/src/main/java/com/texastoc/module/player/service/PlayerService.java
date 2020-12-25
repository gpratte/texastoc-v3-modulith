package com.texastoc.module.player.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.common.AuthorizationHelper;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.exception.CannotRemoveRoleException;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class PlayerService implements PlayerModule {

  private final PlayerRepository playerRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailConnector emailConnector;
  private final AuthorizationHelper authorizationHelper;

  // Only one server so cache the forgot password codes here
  private Map<String, String> forgotPasswordCodes = new HashMap<>();

  public PlayerService(PlayerRepository playerRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailConnector emailConnector, AuthorizationHelper authorizationHelper) {
    this.playerRepository = playerRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.emailConnector = emailConnector;
    this.authorizationHelper = authorizationHelper;
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
        .type(Role.Type.USER)
        .build()))
      .build();

    int id = playerRepository.save(playerToCreate).getId();

    player.setId(id);
    return player;
  }

  @Override
  @Transactional
  public void update(Player player) {
    verifyLoggedInUserIsAdminOrSelf(player);
    Player existingPlayer = playerRepository.findById(player.getId()).get();
    player.setPassword(existingPlayer.getPassword());
    player.setRoles((existingPlayer.getRoles()));
    playerRepository.save(player);
  }

  @Override
  @Transactional
  public void updatePassword(int id, String newPassword) {
    Player existingPlayer = playerRepository.findById(id).get();
    verifyLoggedInUserIsAdminOrSelf(existingPlayer);
    existingPlayer.setPassword(bCryptPasswordEncoder.encode(newPassword));
    playerRepository.save(existingPlayer);
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
  public Player get(int id) throws NotFoundException {
    Optional<Player> optionalPlayer = playerRepository.findById(id);
    if (!optionalPlayer.isPresent()) {
      throw new NotFoundException("Player with id " + id + " not found");
    }
    Player player = optionalPlayer.get();
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
    verifyLoggedInUserIsAdmin();
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

  @Override
  public void addRole(int id, Role role) {
    verifyLoggedInUserIsAdmin();
    Player existingPlayer = get(id);
    // Check that role is not already set
    for (Role existingRole : existingPlayer.getRoles()) {
      if (existingRole.getType() == role.getType()) {
        return;
      }
    }
    existingPlayer.getRoles().add(role);
    playerRepository.save(existingPlayer);
  }

  @Override
  public void removeRole(int id, int roleId) {
    verifyLoggedInUserIsAdmin();
    Player existingPlayer = get(id);
    // Check that role is set
    boolean found = false;
    Set<Role> existingRoles = existingPlayer.getRoles();
    for (Role existingRole : existingRoles) {
      if (existingRole.getId() == roleId) {
        found = true;
        break;
      }
    }

    if (!found) {
      throw new NotFoundException("Role with id " + roleId + " not found");
    }

    // found the role, now make sure it is not the only role
    if (existingRoles.size() < 2) {
      throw new CannotRemoveRoleException("Cannot remove role last role");
    }

    Set<Role> newRoles = new HashSet<>();
    for (Role existingRole : existingRoles) {
      if (existingRole.getId() != roleId) {
        newRoles.add(existingRole);
      }
    }

    existingPlayer.setRoles(newRoles);
    playerRepository.save(existingPlayer);
  }

  // verify the user is an admin
  private void verifyLoggedInUserIsAdmin() {
    if (!authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)) {
      throw new AccessDeniedException("A player that is not an admin cannot update another player");
    }
  }

  // verify the user is either admin or acting upon itself
  private void verifyLoggedInUserIsAdminOrSelf(Player player) {
    if (!authorizationHelper.isLoggedInUserHaveRole(Role.Type.ADMIN)) {
      String email = authorizationHelper.getLoggedInUserEmail();
      List<Player> players = playerRepository.findByEmail(email);
      if (players.size() != 1) {
        throw new NotFoundException("Could not find player with email " + email);
      }
      Player loggedInPlayer = players.get(0);
      if (loggedInPlayer.getId() != player.getId()) {
        throw new AccessDeniedException("A player that is not an admin cannot update another player");
      }
    }
  }
}
