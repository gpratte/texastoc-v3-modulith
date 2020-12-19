package com.texastoc.repository;

import com.texastoc.model.season.Season;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class SeasonRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public SeasonRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String INSERT_SQL = "INSERT INTO season "
    + " (startDate, endDate, finalized, numGames, numGamesPlayed, buyInCost, rebuyAddOnCost, rebuyAddOnTocDebit, tocPerGame, kittyPerGame, quarterlyTocPerGame, quarterlyTocPayouts) "
    + " VALUES "
    + " (:startDate, :endDate, :finalized, :numGames, :numGamesPlayed, :buyInCost, :rebuyAddOnCost, :rebuyAddOnTocDebit, :tocPerGame, :kittyPerGame, :quarterlyTocPerGame, :quarterlyTocPayouts)";

  public int save(Season season) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("startDate", season.getStart());
    params.addValue("endDate", season.getEnd());
    params.addValue("finalized", season.isFinalized());
    params.addValue("numGames", season.getNumGames());
    params.addValue("numGamesPlayed", season.getNumGamesPlayed());
    params.addValue("buyInCost", season.getBuyInCost());
    params.addValue("rebuyAddOnCost", season.getRebuyAddOnCost());
    params.addValue("rebuyAddOnTocDebit", season.getRebuyAddOnTocDebit());
    params.addValue("tocPerGame", season.getTocPerGame());
    params.addValue("kittyPerGame", season.getKittyPerGame());
    params.addValue("quarterlyTocPerGame", season.getQuarterlyTocPerGame());
    params.addValue("quarterlyTocPayouts", season.getQuarterlyNumPayouts());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    //noinspection ConstantConditions
    return keyHolder.getKey().intValue();
  }

  private static final String UPDATE_SQL = "UPDATE season set " +
    "buyInCollected=:buyInCollected, rebuyAddOnCollected=:rebuyAddOnCollected, " +
    "annualTocCollected=:annualTocCollected, totalCollected=:totalCollected, " +
    "annualTocFromRebuyAddOnCalculated=:annualTocFromRebuyAddOnCalculated, " +
    "rebuyAddOnLessAnnualTocCalculated=:rebuyAddOnLessAnnualTocCalculated, " +
    "totalCombinedAnnualTocCalculated=:totalCombinedAnnualTocCalculated, " +
    "kittyCalculated=:kittyCalculated, prizePotCalculated=:prizePotCalculated, " +
    "numGamesPlayed=:numGamesPlayed, lastCalculated=:lastCalculated, " +
    "finalized=:finalized " +
    " where id=:id";

  public void update(final Season season) {

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("buyInCollected", season.getBuyInCollected());
    params.addValue("rebuyAddOnCollected", season.getRebuyAddOnCollected());
    params.addValue("annualTocCollected", season.getAnnualTocCollected());
    params.addValue("totalCollected", season.getTotalCollected());
    params.addValue("annualTocFromRebuyAddOnCalculated", season.getAnnualTocFromRebuyAddOnCalculated());
    params.addValue("rebuyAddOnLessAnnualTocCalculated", season.getRebuyAddOnLessAnnualTocCalculated());
    params.addValue("totalCombinedAnnualTocCalculated", season.getTotalCombinedAnnualTocCalculated());
    params.addValue("kittyCalculated", season.getKittyCalculated());
    params.addValue("prizePotCalculated", season.getPrizePotCalculated());
    params.addValue("numGamesPlayed", season.getNumGamesPlayed());
    params.addValue("lastCalculated", season.getLastCalculated());
    params.addValue("finalized", season.isFinalized());
    params.addValue("id", season.getId());

    jdbcTemplate.update(UPDATE_SQL, params);
  }

  public Season get(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    try {
      return jdbcTemplate
        .queryForObject("select * from season where id = :id", params, new SeasonMapper());
    } catch (Exception e) {
      return null;
    }
  }

  public List<Season> getAll() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    try {
      return jdbcTemplate.query("select * from season", params, new SeasonMapper());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public LocalDateTime getLastCalculated(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    try {
      return jdbcTemplate
        .queryForObject("select lastCalculated from season where id = :id", params, LocalDateTime.class);
    } catch (Exception e) {
      return null;
    }
  }

  public Season getCurrent() {
//    MapSqlParameterSource params = new MapSqlParameterSource();
//    List<Season> seasons = jdbcTemplate.query("select * from season where CURRENT_DATE >= startDate and CURRENT_DATE <= endDate order by id desc", params, new SeasonMapper());
//
//    if (seasons.size() > 0) {
//      return seasons.get(0);
//    }

    throw new IncorrectResultSizeDataAccessException(0);
  }

  public List<Season> getUnfinalized() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    return jdbcTemplate
      .query("select * from season where finalized = false", params, new SeasonMapper());
  }

  public List<Season> getMostRecent() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    return jdbcTemplate
      .query("select * from season order by startDate desc limit 1", params, new SeasonMapper());
  }

  private static final class SeasonMapper implements RowMapper<Season> {
    @Override
    public Season mapRow(ResultSet rs, int rowNum) {
      Season season = new Season();
      try {
        season.setId(rs.getInt("id"));
        season.setStart(rs.getDate("startDate").toLocalDate());
        season.setEnd(rs.getDate("endDate").toLocalDate());
        season.setFinalized(rs.getBoolean("finalized"));
        season.setNumGames(rs.getInt("numGames"));
        season.setNumGamesPlayed(rs.getInt("numGamesPlayed"));
        season.setBuyInCost(rs.getInt("buyInCost"));
        season.setRebuyAddOnCost(rs.getInt("rebuyAddOnCost"));
        season.setRebuyAddOnTocDebit(rs.getInt("rebuyAddOnTocDebit"));
        season.setTocPerGame(rs.getInt("tocPerGame"));
        season.setKittyPerGame(rs.getInt("kittyPerGame"));
        season.setQuarterlyTocPerGame(rs.getInt("quarterlyTocPerGame"));
        season.setQuarterlyNumPayouts(rs.getInt("quarterlyTocPayouts"));
        season.setBuyInCollected(rs.getInt("buyInCollected"));
        season.setRebuyAddOnCollected(rs.getInt("rebuyAddOnCollected"));
        season.setAnnualTocCollected(rs.getInt("annualTocCollected"));
        season.setTotalCollected(rs.getInt("totalCollected"));
        season.setAnnualTocFromRebuyAddOnCalculated(rs.getInt("annualTocFromRebuyAddOnCalculated"));
        season.setRebuyAddOnLessAnnualTocCalculated(rs.getInt("rebuyAddOnLessAnnualTocCalculated"));
        season.setTotalCombinedAnnualTocCalculated(rs.getInt("totalCombinedAnnualTocCalculated"));
        season.setKittyCalculated(rs.getInt("kittyCalculated"));
        season.setPrizePotCalculated(rs.getInt("prizePotCalculated"));

        Timestamp lastCalculated = rs.getTimestamp("lastCalculated");
        if (lastCalculated != null) {
          season.setLastCalculated(lastCalculated.toLocalDateTime());
        }
      } catch (SQLException e) {
        log.error("Error mapping table to object", e);
      }
      return season;
    }
  }

}
