package com.texastoc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.model.system.Settings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
public class SystemRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public SystemRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Settings get() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    return jdbcTemplate
      .queryForObject("select * from settings where id = 1", params, new SettingsMapper());
  }

  private static final class SettingsMapper implements RowMapper<Settings> {
    public Settings mapRow(ResultSet rs, int rowNum) {
      Settings settings = null;
      try {
        String settingsAsJson = rs.getString("settings");
        settings = OBJECT_MAPPER.readValue(settingsAsJson, Settings.class);
      } catch (SQLException | JsonProcessingException e) {
        log.error("Problem mapping Settings", e);
      }

      return settings;
    }
  }

}
