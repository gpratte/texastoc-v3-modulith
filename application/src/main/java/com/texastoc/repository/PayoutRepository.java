package com.texastoc.repository;

import com.texastoc.model.common.Payout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
public class PayoutRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public PayoutRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final HashMap<Integer, List<Payout>> PAYOUTS =
    new HashMap<>();


  public List<Payout> get(int num) {

    if (PAYOUTS.get(num) != null) {
      return PAYOUTS.get(num);
    }

    // Get all the payouts and cache them in PAYOUTS
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("numPayouts", num);

    List<Payout> payouts = jdbcTemplate.query("select * from payout where numPayouts = :numPayouts order by numPayouts, place", params, new PayoutMapper());

    for (Payout payout : payouts) {
      List<Payout> payoutsForPlaces = PAYOUTS.get(num);
      //noinspection Java8MapApi
      if (payoutsForPlaces == null) {
        payoutsForPlaces = new ArrayList<>(num);
        PAYOUTS.put(num, payoutsForPlaces);
      }
      payoutsForPlaces.add(payout);
    }

    return PAYOUTS.get(num);
  }

  private static final class PayoutMapper implements RowMapper<Payout> {
    public Payout mapRow(ResultSet rs, int rowNum) {
      Payout payout = null;
      try {
        payout = Payout.builder()
          .numPayouts(rs.getInt("numPayouts"))
          .place(rs.getInt("place"))
          .percent(rs.getDouble("percent"))
          .build();
      } catch (SQLException e) {
        log.error("Problem mapping TocConfig", e);
      }
      return payout;
    }
  }
}
