package com.texastoc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.texastoc.model.season.SeasonPayoutRange;
import com.texastoc.model.season.SeasonPayoutSettings;
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
public class SeasonPayoutSettingsRepository {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final NamedParameterJdbcTemplate jdbcTemplate;

  public SeasonPayoutSettingsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public SeasonPayoutSettings getBySeasonId(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    return jdbcTemplate
      .queryForObject("select * from seasonpayoutsettings where seasonId = :id",
        params,
        new SeasonPayoutSettingsMapper());
  }

  private static final class SeasonPayoutSettingsMapper implements RowMapper<SeasonPayoutSettings> {
    public SeasonPayoutSettings mapRow(ResultSet rs, int rowNum) {
      SeasonPayoutSettings seasonPayoutSettings = new SeasonPayoutSettings();
      try {
        seasonPayoutSettings.setId(rs.getInt("id"));
        seasonPayoutSettings.setSeasonId(rs.getInt("seasonId"));
        List<SeasonPayoutRange> ranges = MAPPER.readValue(rs.getString("settings"),
          new TypeReference<List<SeasonPayoutRange>>() {
          });
        seasonPayoutSettings.setRanges(ranges);
      } catch (SQLException | JsonProcessingException e) {
        log.error("Problem mapping SeasonPayoutSettings", e);
      }

      return seasonPayoutSettings;
    }
  }

}
