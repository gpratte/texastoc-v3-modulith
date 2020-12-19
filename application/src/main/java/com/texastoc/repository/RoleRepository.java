package com.texastoc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class RoleRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private static final int USER_ROLE = 2;

  public RoleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String INSERT_SQL = "INSERT INTO player_roles "
    + " (playerId, roleId) "
    + " VALUES "
    + " (:playerId, :roleId) ";

  public void save(int playerId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("playerId", playerId);

    // Hardcoded to USER ROLE
    params.addValue("roleId", USER_ROLE);

    jdbcTemplate.update(INSERT_SQL, params);
  }

}
