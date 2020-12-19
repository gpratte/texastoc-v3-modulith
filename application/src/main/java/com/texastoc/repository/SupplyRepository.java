package com.texastoc.repository;

import com.texastoc.model.supply.Supply;
import com.texastoc.model.supply.SupplyType;
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
public class SupplyRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public SupplyRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Supply> get() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    return jdbcTemplate
      .query("select * from supply",
        params,
        new SupplyMapper());
  }

  private static final String INSERT_SQL = "INSERT INTO supply "
    + " (date, type, amount, description) "
    + " VALUES "
    + " (:date, :type, :amount, :description)";

  public void save(Supply supply) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("date", supply.getDate());
    params.addValue("type", supply.getType().name());
    params.addValue("amount", supply.getAmount());
    params.addValue("description", supply.getDescription());

    jdbcTemplate.update(INSERT_SQL, params);
  }

  private static final class SupplyMapper implements RowMapper<Supply> {
    public Supply mapRow(ResultSet rs, int rowNum) {
      Supply supply = new Supply();
      try {
        supply.setId(rs.getInt("id"));
        supply.setAmount(rs.getInt("amount"));
        supply.setDate(rs.getDate("date").toLocalDate());
        supply.setDescription(rs.getString("description"));
        supply.setType(SupplyType.valueOf(rs.getString("type")));
      } catch (SQLException e) {
        log.error("Problem mapping Supply", e);
      }

      return supply;
    }
  }

}
