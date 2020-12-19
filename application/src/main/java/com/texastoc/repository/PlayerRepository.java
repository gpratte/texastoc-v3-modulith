package com.texastoc.repository;

import com.texastoc.model.user.Player;
import com.texastoc.model.user.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class PlayerRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public PlayerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // Does not include the players' roles or password
  public List<Player> get() {
    List<Player> players = jdbcTemplate.query("select id, firstName, lastName, phone, email from player order by firstName, lastName", new PlayerMapper());
    Collections.sort(players);
    return players;
  }

  private static final String GET_SQL = "select pl.*, " +
    "r.name, r.description, r.id as roleId " +
    "from player pl " +
    "left join player_roles on pl.id = player_roles.playerId " +
    "left join role r on r.id = player_roles.roleId " +
    "where pl.id = :id";

  public Player get(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    return jdbcTemplate.query(GET_SQL, params, new PlayerResultSetExtractor());
  }

  private static final String GET_BY_EMAIL_SQL = "select pl.*, " +
    "r.name, r.description, r.id as roleId " +
    "from player pl " +
    "left join player_roles on pl.id = player_roles.playerId " +
    "left join role r on r.id = player_roles.roleId " +
    "where pl.email = :email";

  public Player getByEmail(String email) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("email", email);

    return jdbcTemplate.query(GET_BY_EMAIL_SQL, params, new PlayerResultSetExtractor());
  }

  private static final String UPDATE_SQL = "UPDATE player set " +
    "firstName=:firstName, lastName=:lastName, phone=:phone, " +
    "email=:email, password=:password where id=:id";

  public void update(Player player) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("firstName", player.getFirstName());
    params.addValue("lastName", player.getLastName());
    params.addValue("phone", player.getPhone());
    params.addValue("email", player.getEmail());
    params.addValue("password", player.getPassword());
    params.addValue("id", player.getId());

    jdbcTemplate.update(UPDATE_SQL, params);
  }

  private static final String INSERT_SQL = "INSERT INTO player "
    + " (firstName, lastName, phone, email, password) "
    + " VALUES "
    + " (:firstName, :lastName, :phone, :email, :password) ";

  public int save(final Player player) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("firstName", player.getFirstName());
    params.addValue("lastName", player.getLastName());
    params.addValue("phone", player.getPhone());
    params.addValue("email", player.getEmail());
    params.addValue("password", player.getPassword());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    //noinspection ConstantConditions
    return keyHolder.getKey().intValue();
  }

  public void deleteRoleById(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("playerId", id);
    jdbcTemplate
      .update("delete from player_roles where playerId = :playerId", params);
  }

  public void deleteById(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    jdbcTemplate
      .update("delete from player where id = :id", params);
  }

  private static final class PlayerMapper implements RowMapper<Player> {
    public Player mapRow(ResultSet rs, int rowNum) {
      Player player = new Player();
      try {
        player.setId(rs.getInt("id"));
        String value = rs.getString("firstName");
        if (value != null) {
          player.setFirstName(value);
        }

        value = rs.getString("lastName");
        if (value != null) {
          player.setLastName(value);
        }

        value = rs.getString("phone");
        if (value != null) {
          player.setPhone(value);
        }

        value = rs.getString("email");
        if (value != null) {
          player.setEmail(value);
        }

        try {
          value = rs.getString("password");
          if (value != null) {
            player.setPassword(value);
          }
        } catch (SQLException e) {
          // do nothing
        }
      } catch (SQLException e) {
        log.error("Problem mapping player", e);
      }
      return player;
    }
  }

  private static final class PlayerResultSetExtractor implements ResultSetExtractor<Player> {

    @Override
    public Player extractData(ResultSet rs) throws SQLException, DataAccessException {
      Player player = new Player();
      Set<Role> roles = new HashSet<>();
      player.setRoles(roles);

      while (rs.next()) {
        if (player.getId() == 0) {
          player.setId(rs.getInt("id"));
          player.setFirstName(rs.getString("firstName"));
          player.setLastName(rs.getString("lastName"));
          player.setPhone(rs.getString("phone"));
          player.setEmail(rs.getString("email"));
          player.setPassword(rs.getString("password"));
        }

        String roleName = rs.getString("name");
        if (roleName != null) {
          Role role = Role.builder()
            .id(rs.getLong("roleId"))
            .description(rs.getString("description"))
            .name(rs.getString("name"))
            .build();
          roles.add(role);
        }
      }
      return player;
    }
  }
}
