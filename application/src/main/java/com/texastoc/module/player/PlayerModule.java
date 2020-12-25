package com.texastoc.module.player;

import com.texastoc.module.player.model.Player;

import java.util.List;

public interface PlayerModule {
  /**
   * Create a new player
   * @param player
   * @return
   */
  Player create(Player player);

  /**
   * Update a player. Any player can update itself but only an admin can update another player.
   * Only the following fields can be updated
   * <ul>
   *   <li>firstName</li>
   *   <li>lastName</li>
   *   <li>phone</li>
   *   <li>email</li>
   * </ul>
   * @param player
   */
  void update(Player player);

  // TODO add/remove role, update password

  /**
   * Get all players
   * @return
   */
  List<Player> getAll();

  /**
   * Get a single player
   * @param id
   * @return
   */
  Player get(int id);

  /**
   * Delete a player can only be done by an admin
   * @param id
   */
  void delete(int id);

  /**
   * An email will be sent with a code
   * @param email the email of the player that forget the password
   */
  void forgotPassword(String email);

  /**
   * The code sent from the forgotPassword method and a new password
   * @param code
   * @param password
   */
  void resetPassword(String code, String password);
}
