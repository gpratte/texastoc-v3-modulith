package com.texastoc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.model.game.Seating;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class SeatingRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public SeatingRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Seating get(int gameId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", gameId);
    List<Seating> seatings = jdbcTemplate
      .query("select * from seating where gameId = :gameId",
        params,
        new SeatingMapper());
    if (seatings.size() == 1) {
      return seatings.get(0);
    }
    return new Seating();
  }

  public void deleteByGameId(int gameId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", gameId);

    jdbcTemplate
      .update("delete from seating where gameId = :gameId", params);
  }

  private static final String INSERT_SEAT_SQL = "INSERT INTO seating "
    + " (gameId, settings) VALUES (:gameId, :settings)";

  public void save(Seating seating) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", seating.getGameId());
    try {
      String seatingAsJson = OBJECT_MAPPER.writeValueAsString(seating);
      params.addValue("settings", OBJECT_MAPPER.writeValueAsString(seating));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    jdbcTemplate.update(INSERT_SEAT_SQL, params);
  }

  private static final String UPDATE_SQL = "UPDATE seating set " +
    "settings=:settings where gameId=:gameId";

  public void update(final Seating seating) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    try {
      params.addValue("settings", OBJECT_MAPPER.writeValueAsString(seating));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    params.addValue("gameId", seating.getGameId());
    jdbcTemplate.update(UPDATE_SQL, params);
  }

  private static final class SeatingMapper implements RowMapper<Seating> {
    public Seating mapRow(ResultSet rs, int rowNum) throws SQLException {
      String settings = rs.getString("settings");
      try {
        return OBJECT_MAPPER.readValue(settings, Seating.class);
      } catch (JsonProcessingException e) {
        throw new SQLException("Could not serialize the seating json", e);
      }
    }
  }
}
