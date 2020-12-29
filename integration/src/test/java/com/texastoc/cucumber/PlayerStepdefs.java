package com.texastoc.cucumber;

import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
//@Ignore
public class PlayerStepdefs extends SpringBootBaseIntegrationTest {

  Player playerToCreate;
  Player anotherPlayerToCreate;
  Player playerCreated;
  Player anotherPlayerCreated;
  Player updatePlayer;
  Player playerRetrieved;
  List<Player> playersRetrieved;
  Exception exception;

  @Before
  public void before() {
    playerToCreate = null;
    anotherPlayerToCreate = null;
    playerCreated = null;
    anotherPlayerCreated = null;
    updatePlayer = null;
    playerRetrieved = null;
    playersRetrieved = null;
    exception = null;
  }

  @Given("^a new player$")
  public void a_new_player() throws Exception {
    playerToCreate = Player.builder()
      .firstName("John")
      .lastName("Luther")
      .email(UUID.randomUUID() + "@bbc.com")
      // 2732833 = created on the phone keyboard
      .phone("2732833")
      .password("password")
      .build();
    playerCreated = createPlayer(playerToCreate, login(ADMIN_EMAIL, ADMIN_PASSWORD));
  }

  @Given("^another new player$")
  public void anotherNewPlayer() throws Exception {
    anotherPlayerToCreate = Player.builder()
      .firstName("Jane")
      .lastName("Rain")
      .build();
    anotherPlayerCreated = createPlayer(anotherPlayerToCreate, login(ADMIN_EMAIL, ADMIN_PASSWORD));
  }

  @When("^the player is updated$")
  public void the_player_is_updated() throws Exception {
    updatePlayer = Player.builder()
      .id(playerCreated.getId())
      .firstName("updated_" + playerCreated.getFirstName())
      .lastName("updated_" + playerCreated.getLastName())
      .email("updated_" + playerCreated.getEmail())
      // 8732833 = updated on phone keyboard
      .phone("8732833z")
      .password("updated_" + "password")
      .build();

    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    updatePlayer(updatePlayer, token);
  }

  @When("^the player is updated by another player$")
  public void playerUpdatedByAnother() throws Exception {
    updatePlayer = Player.builder()
      .id(playerCreated.getId())
      .firstName("updated_" + playerCreated.getFirstName())
      .lastName("updated_" + playerCreated.getLastName())
      .email("updated_" + playerCreated.getEmail())
      // 8732833 = updated on phone keyboard
      .phone("8732833z")
      .password("updated_" + "password")
      .build();

    String token = login(USER_EMAIL, USER_PASSWORD);
    try {
      updatePlayer(updatePlayer, token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("^the player is deleted$")
  public void playerDeleted() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    deletePlayer(playerCreated.getId(), token);
  }

  @When("^the player is deleted by another player$")
  public void playerDeletedByAnother() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    try {
      deletePlayer(playerCreated.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("^the player is retrieved$")
  public void getPlayer() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    try {
      playerRetrieved = getPlayer(playerCreated.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("^a role is added$")
  public void addRole() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    addRole(playerCreated.getId(), Role.builder()
      .type(Role.Type.ADMIN)
      .build(),
      token);
  }

  @When("^a role is added by another player$")
  public void addRoleByAnother() throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    try {
      addRole(playerCreated.getId(), Role.builder()
          .type(Role.Type.ADMIN)
          .build(),
        token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("^a role is removed$")
  public void removeRole() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    Role role = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false)
      .filter(r -> r.getType() == Role.Type.ADMIN)
      .findFirst().get();

    removeRole(playerCreated.getId(), role.getId(), token);
  }

  @When("^a role is removed by another player$")
  public void removeRoleByAnother() throws Exception {
    Role role = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false)
      .filter(r -> r.getType() == Role.Type.ADMIN)
      .findFirst().get();

    try {
      String token = login(USER_EMAIL, USER_PASSWORD);
      removeRole(playerCreated.getId(), role.getId(), token);
    } catch (Exception e) {
      exception = e;
    }
  }

  @When("^the players are retrieved$")
  public void getPlayers() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    playersRetrieved = getPlayers(token);
  }

  @Then("^the player matches$")
  public void thePlayerMatches() throws Exception {
    playerMatches(playerToCreate, playerRetrieved);
  }

  @Then("^the updated player matches$")
  public void theUpdatePlayerMatches() throws Exception {
    playerMatches(updatePlayer, playerRetrieved);
  }

  @Then("^the player has one role$")
  public void playerHasOneRole() throws Exception {
    List<Role> roles = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false).collect(Collectors.toList());
    assertEquals("should only have one role", 1, roles.size());
    assertEquals("should be USER role", Role.Type.USER, roles.get(0).getType());
  }

  @Then("^the player has two roles$")
  public void playerHasTwoRoles() throws Exception {
    List<Role> roles = StreamSupport.stream(playerRetrieved.getRoles().spliterator(), false).collect(Collectors.toList());
    assertEquals("should only have two roles", 2, roles.size());
    List<Role.Type> types = roles.stream().map(Role::getType).collect(Collectors.toList());
    assertThat(types).containsExactlyInAnyOrder(Role.Type.USER, Role.Type.ADMIN);
  }


  @Then("^a forbidden error happens$")
  public void checkForbidden() throws Exception {
    assertTrue("exception should be HttpClientErrorException", (exception instanceof HttpClientErrorException));
    HttpClientErrorException e = (HttpClientErrorException)exception;
    assertEquals("status should be forbidden", HttpStatus.FORBIDDEN, e.getStatusCode());
  }

  @Then("^a not found error happens$")
  public void checkNotFound() throws Exception {
    assertTrue("exception should be HttpClientErrorException", (exception instanceof HttpClientErrorException));
    HttpClientErrorException e = (HttpClientErrorException)exception;
    assertEquals("status should be not found", HttpStatus.NOT_FOUND, e.getStatusCode());
  }

  private void playerMatches(Player request, Player response) throws Exception {
    assertEquals("first name should match", request.getFirstName(), response.getFirstName());
    assertEquals("last name should match", request.getLastName(), response.getLastName());
    assertEquals("phone should match", request.getPhone(), response.getPhone());
    assertEquals("email should match", request.getEmail(), response.getEmail());
    assertNull("password should be null", response.getPassword());
    assertEquals("should only have one role", 1, response.getRoles().size());

    List<Role> roles = StreamSupport.stream(response.getRoles().spliterator(), false).collect(Collectors.toList());
    assertEquals("should only have one role", 1, roles.size());
    assertEquals("should be USER role", Role.Type.USER, roles.get(0).getType());
  }

  @Then("^the players match$")
  public void thePlayersMatch() throws Exception {
    boolean firstMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == playerCreated.getId()) {
        assertEquals("first name should match", playerToCreate.getFirstName(), player.getFirstName());
        assertEquals("last name should match", playerToCreate.getLastName(), player.getLastName());
        firstMatch = true;
      }
    }
    assertTrue("should have returned the first player created", firstMatch);

    boolean secondMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == anotherPlayerCreated.getId()) {
        assertEquals("first name should match", anotherPlayerToCreate.getFirstName(), player.getFirstName());
        assertEquals("last name should match", anotherPlayerToCreate.getLastName(), player.getLastName());
        secondMatch = true;
      }
    }
    assertTrue("should have returned the second player created", secondMatch);
  }

  @Then("^the player has the expected encoded password$")
  public void the_player_has_the_expected_encoded_password() throws Exception {
    assertNotNull("player retrieved not null", playerRetrieved);
    assertEquals("id match", playerRetrieved.getId(), playerCreated.getId());
    assertEquals("first name match", updatePlayer.getFirstName(), playerRetrieved.getFirstName());
    assertEquals("last name match", updatePlayer.getLastName(), playerRetrieved.getLastName());
    assertEquals("email match", updatePlayer.getEmail(), playerRetrieved.getEmail());
    assertEquals("phone match", updatePlayer.getPhone(), playerRetrieved.getPhone());
  }

}
