package com.texastoc.module.player.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.TestConstants;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.player.repository.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
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
  private EmailConnector emailConnector = mock(EmailConnector.class);

  @Before
  public void before() {
    bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    playerRepository = mock(PlayerRepository.class);
    playerService = new PlayerService(playerRepository, bCryptPasswordEncoder, emailConnector);
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
  public void testUpdatePlayer() {
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

}
