package com.texastoc.cucumber;

import com.texastoc.model.user.Player;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Ignore;

import java.util.List;

// Tests are run from SpringBootBaseIntegrationTest so must Ignore here
@Ignore
public class PlayerStepdefs extends SpringBootBaseIntegrationTest {

  Player playerToCreate;
  Player anotherPlayerToCreate;
  Player playerCreated;
  Player anotherPlayerCreated;
  Player updatePlayer;
  Player playerRetrieved;
  List<Player> playersRetrieved;
  String token;

  @Before
  public void before() {
    playerToCreate = null;
    anotherPlayerToCreate = null;
    playerCreated = null;
    anotherPlayerCreated = null;
    updatePlayer = null;
    playerRetrieved = null;
    playersRetrieved = null;
    token = null;
  }

  @Given("^a new player$")
  public void a_new_player() throws Exception {
    playerToCreate = Player.builder()
      .firstName("John")
      .lastName("Luther")
      .build();
    playerCreated = createPlayer(playerToCreate);
  }

  @Given("^another new player$")
  public void anotherNewPlayer() throws Exception {
    anotherPlayerToCreate = Player.builder()
      .firstName("Jane")
      .lastName("Rain")
      .build();
    anotherPlayerCreated = createPlayer(anotherPlayerToCreate);
  }

  @Given("^a new player with email and password$")
  public void a_new_player_with_email_and_password() throws Exception {
    playerToCreate = Player.builder()
      .firstName("John")
      .lastName("Luther")
      .email("john.luther@example.com")
      .password("jacket")
      .build();

    playerCreated = createPlayer(playerToCreate);
  }

  @When("^the player password is updated$")
  public void the_player_password_is_updated() throws Exception {
    updatePlayer = Player.builder()
      .id(playerCreated.getId())
      .firstName(playerCreated.getFirstName())
      .lastName(playerCreated.getLastName())
      .email("abc@rst.com")
      .phone("2344322345")
      .password("password")
      .build();

    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    updatePlayer(updatePlayer, token);
  }

  @When("^the player self retrieves$")
  public void the_player_is_retrieved() throws Exception {
    String token = login("abc@rst.com", "password");
    playerRetrieved = getPlayer(playerCreated.getId(), token);
  }

  @When("^the player is retrieved$")
  public void getPlayer() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    playerRetrieved = getPlayer(playerCreated.getId(), token);
  }

  @When("^the players are retrieved$")
  public void getPlayers() throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    playersRetrieved = getPlayers(token);
  }

  @When("^the player logs in$")
  public void the_player_logs_in() throws Exception {
    token = login(playerToCreate.getEmail(), playerCreated.getPassword());
  }

  @Then("^a token is returned$")
  public void a_token_is_returned() throws Exception {
    Assert.assertNotNull("token not null", token);
  }

  @Then("^the player matches$")
  public void thePlayerMatches() throws Exception {
    Assert.assertEquals("first name should match", playerToCreate.getFirstName(), playerRetrieved.getFirstName());
    Assert.assertEquals("last name should match", playerToCreate.getLastName(), playerRetrieved.getLastName());
  }

  @Then("^the players match$")
  public void thePlayersMatch() throws Exception {
    boolean firstMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == playerCreated.getId()) {
        Assert.assertEquals("first name should match", playerToCreate.getFirstName(), player.getFirstName());
        Assert.assertEquals("last name should match", playerToCreate.getLastName(), player.getLastName());
        firstMatch = true;
      }
    }
    Assert.assertTrue("should have returned the first player created", firstMatch);

    boolean secondMatch = false;
    for (Player player : playersRetrieved) {
      if (player.getId() == anotherPlayerCreated.getId()) {
        Assert.assertEquals("first name should match", anotherPlayerToCreate.getFirstName(), player.getFirstName());
        Assert.assertEquals("last name should match", anotherPlayerToCreate.getLastName(), player.getLastName());
        secondMatch = true;
      }
    }
    Assert.assertTrue("should have returned the second player created", secondMatch);
  }

  @Then("^the player has the expected encoded password$")
  public void the_player_has_the_expected_encoded_password() throws Exception {
    Assert.assertNotNull("player retrieved not null", playerRetrieved);
    Assert.assertEquals("id match", playerRetrieved.getId(), playerCreated.getId());
    Assert.assertEquals("first name match", updatePlayer.getFirstName(), playerRetrieved.getFirstName());
    Assert.assertEquals("last name match", updatePlayer.getLastName(), playerRetrieved.getLastName());
    Assert.assertEquals("email match", updatePlayer.getEmail(), playerRetrieved.getEmail());
    Assert.assertEquals("phone match", updatePlayer.getPhone(), playerRetrieved.getPhone());
  }

}
