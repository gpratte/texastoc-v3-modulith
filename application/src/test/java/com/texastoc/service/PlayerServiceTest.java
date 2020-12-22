package com.texastoc.service;

import com.texastoc.TestConstants;
import com.texastoc.module.game.repository.GamePlayerRepository;
import com.texastoc.module.notification.connector.EmailConnector;
import com.texastoc.module.player.PlayerService;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.repository.PlayerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

@RunWith(SpringRunner.class)
public class PlayerServiceTest implements TestConstants {

  private PlayerService playerService;
  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private PlayerRepository playerRepository;

  @MockBean
  private GamePlayerRepository gamePlayerRepository;

  @MockBean
  BCryptPasswordEncoder bCryptPasswordEncoder;

  @MockBean
  EmailConnector emailConnector;

  @Before
  public void before() {
    playerService = new PlayerService(playerRepository, gamePlayerRepository, bCryptPasswordEncoder, emailConnector);
  }

  @Ignore
  @Test
  public void testCreatePlayer() {

    // Arrange
    Player expected = Player.builder()
      .firstName("bob")
      .lastName("yoursuncle")
      .phone("1234567890")
      .email("abc@xyz.com")
      .build();

//    Mockito.when(playerRepository.save((Player) notNull())).thenReturn(1);

    // Act
    Player actual = playerService.create(expected);

    Mockito.verify(playerRepository, Mockito.times(1)).save(Mockito.any(Player.class));

    // Assert
    Assert.assertNotNull("created player not null", actual);
    Assert.assertEquals("first name same", expected.getFirstName(), actual.getFirstName());
    Assert.assertEquals("last name same", expected.getLastName(), actual.getLastName());
    Assert.assertEquals("phone name same", expected.getPhone(), actual.getPhone());
    Assert.assertEquals("email same", expected.getEmail(), actual.getEmail());
    Assert.assertNull("password null", actual.getPassword());
  }

  @Ignore
  @Test
  public void testUpdatePassword() {

    String newPasswordEncoded = "newpasswordencoded";

    // Arrange
    Player player = Player.builder()
      .id(1)
      .email("abc@xyz.com")
      .password("newpassword")
      .build();

//    Mockito.when(playerRepository.get(ArgumentMatchers.eq(1)))
//      .thenReturn(Player.builder()
//        .password(newPasswordEncoded)
//        .build());

    // Act
    playerService.update(player);
    Player updatedPlayer = playerService.get(1);

//    Mockito.verify(playerRepository, Mockito.times(1)).update(Mockito.any(Player.class));
//    Mockito.verify(playerRepository, Mockito.times(2)).get(1);

    // Assert
    Assert.assertNotNull("updated player not null", updatedPlayer);
    Assert.assertNull("new password null", updatedPlayer.getPassword());
  }

}
