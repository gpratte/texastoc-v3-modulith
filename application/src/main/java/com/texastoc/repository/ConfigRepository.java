package com.texastoc.repository;

import com.texastoc.model.config.TocConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
public class ConfigRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ConfigRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public TocConfig get() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    return jdbcTemplate.queryForObject("select * from tocconfig", params, new TocConfigMapper());
  }

  private static final class TocConfigMapper implements RowMapper<TocConfig> {
    @Override
    public TocConfig mapRow(ResultSet rs, int rowNum) {
      TocConfig tocConfig = null;
      try {
        tocConfig = TocConfig.builder()
          .kittyDebit(rs.getInt("kittyDebit"))
          .annualTocCost(rs.getInt("annualTocCost"))
          .quarterlyTocCost(rs.getInt("quarterlyTocCost"))
          .quarterlyNumPayouts(rs.getInt("quarterlyNumPayouts"))
          .regularBuyInCost(rs.getInt("regularBuyInCost"))
          .regularRebuyCost(rs.getInt("regularRebuyCost"))
          .regularRebuyTocDebit(rs.getInt("regularRebuyTocDebit"))
          .build();
      } catch (SQLException e) {
        log.error("Problem mapping TocConfig", e);
      }
      return tocConfig;
    }
  }
}
