package com.texastoc.repository;

import com.texastoc.model.game.GamePayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class GamePayoutRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public GamePayoutRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public List<GamePayout> getByGameId(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    return jdbcTemplate
      .query("select * from gamepayout where gameId = :id order by amount desc",
        params,
        new GamePayoutMapper());
  }

  private static final String INSERT_SQL =
    "INSERT INTO gamepayout "
      + "(gameId, place, amount, chopAmount, chopPercent) "
      + " VALUES "
      + " (:gameId, :place, :amount, :chopAmount, :chopPercent)";

  public int save(final GamePayout gamePayout) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", gamePayout.getGameId());
    params.addValue("place", gamePayout.getPlace());
    params.addValue("amount", gamePayout.getAmount());
    params.addValue("chopAmount", gamePayout.getChopAmount());
    params.addValue("chopPercent", gamePayout.getChopPercent());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);
    return keyHolder.getKey().intValue();
  }


  public void deleteByGameId(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    jdbcTemplate.update("delete from gamepayout where gameId = :id", params);
  }

  private static final class GamePayoutMapper implements RowMapper<GamePayout> {
    public GamePayout mapRow(ResultSet rs, int rowNum) {
      GamePayout gamePayout = new GamePayout();
      try {
        gamePayout.setId(rs.getInt("id"));
        gamePayout.setGameId(rs.getInt("gameId"));
        gamePayout.setPlace(rs.getInt("place"));

        String temp = rs.getString("amount");
        if (temp != null) {
          gamePayout.setAmount(Integer.valueOf(temp));
        }

        temp = rs.getString("chopAmount");
        if (temp != null) {
          gamePayout.setChopAmount(Integer.valueOf(temp));
        }

        temp = rs.getString("chopPercent");
        if (temp != null) {
          gamePayout.setChopPercent(Double.valueOf(temp));
        }
      } catch (SQLException e) {
        log.error("Problem mapping GamePayout", e);
      }

      return gamePayout;
    }
  }

}
