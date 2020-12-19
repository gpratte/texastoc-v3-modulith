package com.texastoc.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.texastoc.TestConstants;
import com.texastoc.controller.request.*;
import com.texastoc.model.game.*;
import com.texastoc.model.season.Season;
import com.texastoc.model.supply.Supply;
import com.texastoc.model.user.Player;
import lombok.Getter;
import lombok.Setter;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootBaseIntegrationTest implements TestConstants {

  private final String SERVER_URL = "http://localhost";
  private String V2_ENDPOINT;

  @LocalServerPort
  private int port;

  protected RestTemplate restTemplate;

  public SpringBootBaseIntegrationTest() {
    restTemplate = new RestTemplate();
  }

  protected String endpoint() {
    if (V2_ENDPOINT == null) {
      V2_ENDPOINT = SERVER_URL + ":" + port + "/api/v2";
    }
    return V2_ENDPOINT;
  }

  protected String endpointRoot() {
    return SERVER_URL + ":" + port;
  }

  protected LocalDate getSeasonStart() {
    LocalDate now = LocalDate.now();
    LocalDate start = null;
    if (now.getMonthValue() < 5) {
      start = LocalDate.of(now.getYear() - 1, Month.MAY, 1);
    } else {
      start = LocalDate.of(now.getYear(), Month.MAY, 1);
    }
    return start;
  }

  protected Season createSeason(String token) throws Exception {
    return createSeason(getSeasonStart(), token);
  }

  protected Season createSeason(LocalDate start, String token) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String seasonAsJson = mapper.writeValueAsString(start);
    HttpEntity<String> entity = new HttpEntity<>(seasonAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/seasons", entity, Season.class);
  }

  protected Game createGame(CreateGameRequest createGameRequest, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    String createGameRequestAsJson = mapper.writeValueAsString(createGameRequest);
    HttpEntity<String> entity = new HttpEntity<>(createGameRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/games", entity, Game.class);
  }

  protected void updateGame(int gameId, UpdateGameRequest updateGameRequest, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String updateGameRequestAsJson = mapper.writeValueAsString(updateGameRequest);
    HttpEntity<String> entity = new HttpEntity<>(updateGameRequestAsJson, headers);

    restTemplate.put(endpoint() + "/games/" + gameId, entity);
  }

  protected GamePlayer addPlayerToGame(CreateGamePlayerRequest cgpr, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String createGamePlayerRequestAsJson = mapper.writeValueAsString(cgpr);
    HttpEntity<String> entity = new HttpEntity<>(createGamePlayerRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/games/" + cgpr.getGameId() + "/players", entity, GamePlayer.class);
  }

  protected GamePlayer addFirstTimePlayerToGame(FirstTimeGamePlayer firstTimeGamePlayer, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.texastoc.new-player+json");
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String firstTimeGamePlayerRequestAsJson = mapper.writeValueAsString(firstTimeGamePlayer);
    HttpEntity<String> entity = new HttpEntity<>(firstTimeGamePlayerRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/games/" + firstTimeGamePlayer.getGameId() + "/players", entity, GamePlayer.class);
  }

  protected void updatePlayerInGame(int gamePlayerId, UpdateGamePlayerRequest ugpr, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String updateGamePlayerRequestAsJson = mapper.writeValueAsString(ugpr);
    HttpEntity<String> entity = new HttpEntity<>(updateGamePlayerRequestAsJson, headers);

    restTemplate.put(endpoint() + "/games/" + ugpr.getGameId() + "/players/" + gamePlayerId, entity);
  }

  protected void deletePlayerFromGame(int gameId, int gamePlayerId, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Void> response = restTemplate.exchange(
      endpoint() + "/games/" + gameId + "/players/" + gamePlayerId,
      HttpMethod.DELETE,
      entity,
      Void.class);
  }

  protected void finalizeGame(int gameId, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.finalize+json");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    restTemplate.put(endpoint() + "/games/" + gameId, entity);
  }

  protected void createSupply(Supply supply, String token) throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    String supplyAsJson = mapper.writeValueAsString(supply);
    HttpEntity<String> entity = new HttpEntity<>(supplyAsJson, headers);

    restTemplate.postForObject(endpoint() + "/supplies", entity, String.class);
  }

  protected List<Supply> getSupplies(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List<Supply>> response = restTemplate.exchange(
      endpoint() + "/supplies",
      HttpMethod.GET,
      entity,
      new ParameterizedTypeReference<List<Supply>>() {
      });
    return response.getBody();

  }

  protected Seating seatPlayers(int gameId, List<Integer> numSeatsPerTable, List<TableRequest> tableRequests, String token) throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.texastoc.assign-seats+json");
    headers.set("Authorization", "Bearer " + token);

    SeatingRequest seatingRequest = SeatingRequest.builder()
      .gameId(gameId)
      .numSeatsPerTable(numSeatsPerTable)
      .tableRequests(tableRequests)
      .build();

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String seatingRequestAsJson = mapper.writeValueAsString(seatingRequest);
    HttpEntity<String> entity = new HttpEntity<>(seatingRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/games/" + gameId + "/seats", entity, Seating.class);
  }

  protected Player createPlayer(Player player) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String playerRequestAsJson = mapper.writeValueAsString(player);
    HttpEntity<String> entity = new HttpEntity<>(playerRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/players", entity, Player.class);
  }

  protected void updatePlayer(Player player, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String playerRequestAsJson = mapper.writeValueAsString(player);
    HttpEntity<String> entity = new HttpEntity<>(playerRequestAsJson, headers);

    restTemplate.put(endpoint() + "/players/" + player.getId(), entity);
  }

  protected Player getPlayer(int id, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Player> response = restTemplate.exchange(
      endpoint() + "/players/" + id,
      HttpMethod.GET,
      entity,
      Player.class);
    return response.getBody();
  }

  protected List<Player> getPlayers(String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List<Player>> response = restTemplate.exchange(
      endpoint() + "/players",
      HttpMethod.GET,
      entity,
      new ParameterizedTypeReference<List<Player>>() {
      });
    return response.getBody();
  }

  protected Game getGame(int id, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Game> response = restTemplate.exchange(
      endpoint() + "/games/" + id,
      HttpMethod.GET,
      entity,
      Game.class);
    return response.getBody();
  }

  protected Game getCurrentGame(String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.current+json");
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Game> response = restTemplate.exchange(
      endpoint() + "/games",
      HttpMethod.GET,
      entity,
      Game.class);
    return response.getBody();
  }

  protected Season getSeason(int id, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Season> response = restTemplate.exchange(
      endpoint() + "/seasons/" + id,
      HttpMethod.GET,
      entity,
      Season.class);
    return response.getBody();
  }

  protected Season getCurrentSeason(String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Season> response = restTemplate.exchange(
      endpoint() + "/seasons/current",
      HttpMethod.GET,
      entity,
      Season.class);
    return response.getBody();
  }

  protected String login(String email, String password) throws JsonProcessingException {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    LoginParameters loginParameters = new LoginParameters();
    loginParameters.email = email;
    loginParameters.password = password;
    String loginParametersAsJson = mapper.writeValueAsString(loginParameters);
    HttpEntity<String> entity = new HttpEntity<>(loginParametersAsJson, headers);

    String url = endpointRoot() + "/login";
    return restTemplate.postForObject(url, entity, Token.class).getToken();
  }


  @Getter
  @Setter
  private static class LoginParameters {
    String email;
    String password;
  }

  @Getter
  @Setter
  private static class Token {
    String token;
  }

}
