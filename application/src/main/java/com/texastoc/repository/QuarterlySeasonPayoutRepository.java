package com.texastoc.repository;

import com.texastoc.model.season.QuarterlySeasonPayout;
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
public class QuarterlySeasonPayoutRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public QuarterlySeasonPayoutRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public List<QuarterlySeasonPayout> getByQSeasonId(int qSeasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("qSeasonId", qSeasonId);

    return jdbcTemplate
      .query("select * from quarterlyseasonpayout where qSeasonId = :qSeasonId order by amount desc",
        params,
        new QuarterlySeasonPayoutMapper());
  }

  private static final String INSERT_SQL =
    "INSERT INTO quarterlyseasonpayout "
      + "(seasonId, qSeasonId, place, amount) "
      + " VALUES "
      + " (:seasonId, :qSeasonId, :place, :amount)";

  public int save(final QuarterlySeasonPayout payout) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", payout.getSeasonId());
    params.addValue("qSeasonId", payout.getQSeasonId());
    params.addValue("place", payout.getPlace());
    params.addValue("amount", payout.getAmount());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);
    return keyHolder.getKey().intValue();
  }

  public void deleteByQSeasonId(int qSeasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("qSeasonId", qSeasonId);
    jdbcTemplate.update("delete from quarterlyseasonpayout where qSeasonId=:qSeasonId", params);
  }

  private static final class QuarterlySeasonPayoutMapper implements RowMapper<QuarterlySeasonPayout> {
    public QuarterlySeasonPayout mapRow(ResultSet rs, int rowNum) {
      QuarterlySeasonPayout qSeasonPayout = new QuarterlySeasonPayout();
      try {
        qSeasonPayout.setId(rs.getInt("id"));
        qSeasonPayout.setSeasonId(rs.getInt("seasonId"));
        qSeasonPayout.setQSeasonId(rs.getInt("qSeasonId"));
        qSeasonPayout.setPlace(rs.getInt("place"));
        qSeasonPayout.setAmount(rs.getInt("amount"));
      } catch (SQLException e) {
        log.error("Problem mapping GamePayout", e);
      }

      return qSeasonPayout;
    }
  }

}
