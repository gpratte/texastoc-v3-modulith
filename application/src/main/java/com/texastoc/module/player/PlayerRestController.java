package com.texastoc.module.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.texastoc.module.player.exception.CannotDeletePlayerException;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.service.PlayerService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@SuppressWarnings("unused")
@RestController
public class PlayerRestController implements PlayerModule {

  private final PlayerService playerService;

  public PlayerRestController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Override
  @PostMapping("/api/v2/players")
  public Player create(@RequestBody Player player) {
    return playerService.create(player);
  }

  @PutMapping("/api/v2/players/{id}")
  public void update(@PathVariable("id") int id, @RequestBody @Valid Player player, HttpServletRequest request) {
    // TODO move this check to a service method
    if (!request.isUserInRole("ADMIN")) {
      Principal principal = request.getUserPrincipal();
      Player playerThatIsLoggedIn = playerService.getByEmail(principal.getName());
      if (playerThatIsLoggedIn.getId() != id) {
        throw new AccessDeniedException("A player that is not an admin cannot update another player");
      }
    }
    player.setId(id);
    update(player);
  }

  @Override
  public void update(Player player) {
    playerService.update(player);
  }

  @Override
  @GetMapping("/api/v2/players")
  public List<Player> getAll() {
    return playerService.getAll();
  }

  @Override
  @GetMapping("/api/v2/players/{id}")
  public Player get(@PathVariable("id") int id) {
    return playerService.get(id);
  }

  @Override
  // TODO need to check for admin role in service method
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/api/v2/players/{id}")
  public void delete(@PathVariable("id") int id) {
    playerService.delete(id);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-forgot+json")
  public void forgot(@RequestBody Forgot forgot) {
    forgotPassword(forgot.getEmail());
  }

  @Override
  public void forgotPassword(String email) {
    playerService.forgotPassword(email);
  }

  @PostMapping(value = "/password/reset", consumes = "application/vnd.texastoc.password-reset+json")
  public void reset(@RequestBody Reset reset) {
    resetPassword(reset.getCode(), reset.getPassword());
  }

  @Override
  public void resetPassword(String code, String password) {
    playerService.resetPassword(code, password);
  }

  @ExceptionHandler(value = {CannotDeletePlayerException.class})
  protected void handleCannotDeletePlayerException(CannotDeletePlayerException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Forgot {
    private String email;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Reset {
    private String code;
    private String password;
  }

}
