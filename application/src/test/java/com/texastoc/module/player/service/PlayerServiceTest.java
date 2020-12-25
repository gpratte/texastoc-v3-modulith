package com.texastoc.module.player.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.texastoc.TestConstants;
import com.texastoc.common.AuthorizationHelper;
import com.texastoc.common.SecurityRole;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

public class PlayerServiceTest implements TestConstants {

  private PlayerService playerService;
  private Random random = new Random(System.currentTimeMillis());

  private PlayerRepository playerRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  private EmailConnector emailConnector;
  private AuthorizationHelper authorizationHelper;

  @Before
  public void before() {
    bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    playerRepository = mock(PlayerRepository.class);
    emailConnector = mock(EmailConnector.class);
    authorizationHelper = mock(AuthorizationHelper.class);
    playerService = new PlayerService(playerRepository, bCryptPasswordEncoder, emailConnector, authorizationHelper);
  }

  @Test
  public void testCreatePlayer() {
    // Arrange
    Player expected = Player.builder()
      .firstName("bobs")
      .lastName("youruncle")
      .phone("1234567890")
      .email("abc@xyz.com")
      .password("password")
      .build();

    when(playerRepository.save((Player) notNull())).thenReturn(Player.builder().id(1).build());

    when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");

    // Act
    Player actual = playerService.create(expected);

    // Assert
    assertNotNull(actual);
    assertEquals(1, actual.getId());
    verify(playerRepository, Mockito.times(1)).save(any(Player.class));
    verify(bCryptPasswordEncoder, Mockito.times(1)).encode("password");

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("bobs", param.getFirstName());
    assertEquals("youruncle", param.getLastName());
    assertEquals("1234567890", param.getPhone());
    assertEquals("abc@xyz.com", param.getEmail());
    assertEquals("encodedPassword", param.getPassword());
    assertThat(param.getRoles()).containsExactly(Role.builder()
      .name(PlayerRepository.USER)
      .build());
  }

  @Test
  public void testUpdateSelf() {
    testUpdate(SecurityRole.USER);
  }

  @Test
  public void testUpdateAdmin() {
    testUpdate(SecurityRole.ADMIN);
  }

  private void testUpdate(SecurityRole securityRole) {
    // Arrange
    Role existingRole = Role.builder()
      .id(1)
      .name("existingRole")
      .build();
    Player existingPlayer = Player.builder()
      .id(1)
      .firstName("existingFirstName")
      .lastName("existingLastName")
      .email("existing@xyz.com")
      .phone("existingPhone")
      .password("existingEncodedPassword")
      .roles(ImmutableSet.of(existingRole))
      .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1))).thenReturn(java.util.Optional.ofNullable(existingPlayer));

    Player playersNewValues = Player.builder()
      .id(1)
      .firstName("updatedFirstName")
      .lastName("updatedLastName")
      .email("updated@xyz.com")
      .phone("updatedPhone")
      .password("updatedPassword") // will be ignored
      .roles(ImmutableSet.of(Role.builder() // will be ignored
        .name("updatedRole")
        .build()))
      .build();

    // mock out to pass the authorization check
    if (securityRole == SecurityRole.USER) {
      when(authorizationHelper.getLoggedInUserEmail()).thenReturn("existing@xyz.com");
      when(playerRepository.findByEmail("existing@xyz.com")).thenReturn(ImmutableList.of(existingPlayer));
    } else {
      when(authorizationHelper.isLoggedInUserHaveRole(SecurityRole.ADMIN)).thenReturn(true);
    }

    // Act
    playerService.update(playersNewValues);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(bCryptPasswordEncoder, Mockito.times(0)).encode(any());
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("updatedFirstName", param.getFirstName());
    assertEquals("updatedLastName", param.getLastName());
    assertEquals("updatedPhone", param.getPhone());
    assertEquals("updated@xyz.com", param.getEmail());

    // password should not change
    assertEquals("existingEncodedPassword", param.getPassword());
    // roles should not change
    assertThat(param.getRoles()).containsExactly(existingRole);
  }

  @Test
  public void testUpdateNotAllowed() {
    // Arrange
    Role existingRole = Role.builder()
      .id(1)
      .name("existingRole")
      .build();
    Player existingPlayer = Player.builder()
      .id(1)
      .firstName("existingFirstName")
      .lastName("existingLastName")
      .email("existing@xyz.com")
      .phone("existingPhone")
      .password("existingEncodedPassword")
      .roles(ImmutableSet.of(existingRole))
      .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1))).thenReturn(java.util.Optional.ofNullable(existingPlayer));

    // different user
    Player playersNewValues = Player.builder()
      .id(2)
      .firstName("updatedFirstName")
      .lastName("updatedLastName")
      .email("updated@xyz.com")
      .phone("updatedPhone")
      .password("updatedPassword") // will be ignored
      .roles(ImmutableSet.of(Role.builder() // will be ignored
        .name("updatedRole")
        .build()))
      .build();

    // mock out authorization check
    when(authorizationHelper.getLoggedInUserEmail()).thenReturn("existing@xyz.com");
    when(playerRepository.findByEmail("existing@xyz.com")).thenReturn(ImmutableList.of(existingPlayer));

    // Act
    assertThatThrownBy(() -> {
      playerService.update(playersNewValues);
    }).isInstanceOf(AccessDeniedException.class)
      .hasMessageContaining("A player that is not an admin cannot update another player");
  }

  @Test
  public void testUpdateSelfPassword() {
    // Arrange
    Role existingRole = Role.builder()
      .id(1)
      .name("existingRole")
      .build();
    Player existingPlayer = Player.builder()
      .id(1)
      .firstName("existingFirstName")
      .lastName("existingLastName")
      .email("existing@xyz.com")
      .phone("existingPhone")
      .password("existingEncodedPassword")
      .roles(ImmutableSet.of(existingRole))
      .build();
    when(playerRepository.findById(ArgumentMatchers.eq(1))).thenReturn(java.util.Optional.ofNullable(existingPlayer));

    Player playersNewValues = Player.builder()
      .id(1)
      .firstName("updatedFirstName")
      .lastName("updatedLastName")
      .email("updated@xyz.com")
      .phone("updatedPhone")
      .password("updatedPassword") // will be ignored
      .roles(ImmutableSet.of(Role.builder() // will be ignored
        .name("updatedRole")
        .build()))
      .build();

    // mock out to pass the authorization check
    when(authorizationHelper.getLoggedInUserEmail()).thenReturn("existing@xyz.com");
    when(playerRepository.findByEmail("existing@xyz.com")).thenReturn(ImmutableList.of(existingPlayer));

    // Act
    playerService.update(playersNewValues);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
    Mockito.verify(bCryptPasswordEncoder, Mockito.times(0)).encode(any());
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));

    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerRepository).save(argument.capture());
    Player param = argument.getValue();
    assertEquals("updatedFirstName", param.getFirstName());
    assertEquals("updatedLastName", param.getLastName());
    assertEquals("updatedPhone", param.getPhone());
    assertEquals("updated@xyz.com", param.getEmail());

    // password should not change
    assertEquals("existingEncodedPassword", param.getPassword());
    // roles should not change
    assertThat(param.getRoles()).containsExactly(existingRole);
  }

  @Test
  public void testGetAll() {
    // Arrange
    Player player2 = Player.builder()
      .id(1)
      .firstName("firstName2")
      .lastName("lastName2")
      .build();
    Player player1 = Player.builder()
      .id(1)
      .firstName("firstName1")
      .lastName("lastName1")
      .build();
    when(playerRepository.findAll()).thenReturn(ImmutableSet.of(player2, player1));

    // Act
    List<Player> players = playerService.getAll();

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findAll();

    // Should be sorted with player1 before player2
    assertThat(players).containsExactly(player1, player2);
  }

  @Test
  public void testGetNotFound() {
    // Arrange
    when(playerRepository.findById(123)).thenReturn(Optional.empty());

    // Act and Assert
    assertThatThrownBy(() -> {
      playerService.get(123);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Player with id 123 not found");
  }

  @Test
  public void testGetFound() {
    // Arrange
    Player player = Player.builder()
      .id(1)
      .firstName("firstName1")
      .lastName("lastName1")
      .build();
    when(playerRepository.findById(1)).thenReturn(Optional.of(player));

    // Act
    Player playerRetrieved = playerService.get(1);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findById(1);
  }

  @Test
  public void testGetByEmailNotFound() {
    // Arrange
    when(playerRepository.findByEmail(any())).thenReturn(Collections.emptyList());

    // Act and Assert
    assertThatThrownBy(() -> {
      playerService.getByEmail("abc");
    }).isInstanceOf(NotFoundException.class)
      .hasMessageStartingWith("Could not find player with email");
  }

  @Test
  public void testGetByEmailFound() {
    // Arrange
    String email = "abc@def.com";
    Player player = Player.builder()
      .id(1)
      .firstName("firstName1")
      .lastName("lastName1")
      .email(email)
      .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));

    // Act
    Player playerRetrieved = playerService.getByEmail(email);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findByEmail(email);
    assertEquals(email, playerRetrieved.getEmail());
  }

  @Test
  public void testDeleteByAdmin() {
    // Arrange
    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(SecurityRole.ADMIN)).thenReturn(true);

    // Act
    playerService.delete(1);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).deleteById(1);
  }

  @Test
  public void testDeleteByNonAdmin() {
    // Arrange
    // mock out to pass the authorization check
    when(authorizationHelper.isLoggedInUserHaveRole(SecurityRole.ADMIN)).thenReturn(false);

    // Act
    assertThatThrownBy(() -> {
      playerService.delete(1);
    }).isInstanceOf(AccessDeniedException.class)
      .hasMessageContaining("A player that is not an admin cannot update another player");
  }

  @Test
  public void testForgotPassword() {
    // Act
    playerService.forgotPassword("abc@def.com");

    // Assert
    Mockito.verify(emailConnector, Mockito.times(1)).send(anyString(), anyString(), anyString());
  }

  @Test
  public void testResetPassword() {
    // Arrange
    String email = "abc@def.com";
    String password = "newPassword";

    playerService.forgotPassword(email);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(emailConnector).send(anyString(), anyString(), argument.capture());
    String code = argument.getValue();
    System.out.println(code);

    Player player = Player.builder()
      .id(1)
      .firstName("firstName1")
      .lastName("lastName1")
      .email(email)
      .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));


    // Act
    playerService.resetPassword(code, password);

    // Assert
    Mockito.verify(playerRepository, Mockito.times(1)).findByEmail(email);
    Mockito.verify(bCryptPasswordEncoder, Mockito.times(1)).encode(password);
    Mockito.verify(playerRepository, Mockito.times(1)).save(any(Player.class));
  }

  @Test
  public void testResetPasswordNotFound() {
    // Arrange
    String email = "abc@def.com";
    String password = "newPassword";

    playerService.forgotPassword(email);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(emailConnector).send(anyString(), anyString(), argument.capture());
    String code = argument.getValue();
    System.out.println(code);

    Player player = Player.builder()
      .id(1)
      .firstName("firstName1")
      .lastName("lastName1")
      .email(email)
      .build();
    when(playerRepository.findByEmail(email)).thenReturn(ImmutableList.of(player));


    // Act
    playerService.resetPassword(code, password);

    // Cannot use the same code twice
    assertThatThrownBy(() -> {
      playerService.resetPassword(code, password);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("No code found");
  }
}
