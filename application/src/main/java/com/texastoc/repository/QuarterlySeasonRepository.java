package com.texastoc.repository;

import com.texastoc.model.season.Quarter;
import com.texastoc.model.season.QuarterlySeason;
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
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
public class QuarterlySeasonRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public QuarterlySeasonRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String INSERT_SQL = "INSERT INTO quarterlyseason "
    + " (seasonId, startDate, endDate, finalized, quarter, numGames, numGamesPlayed, qTocCollected, qTocPerGame, numPayouts) "
    + " VALUES "
    + " (:seasonId, :startDate, :endDate, :finalized, :quarter, :numGames, :numGamesPlayed, :qTocCollected, :qTocPerGame, :numPayouts)";

  @SuppressWarnings("Duplicates")
  public int save(QuarterlySeason quarterlySeason) {

    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", quarterlySeason.getSeasonId());
    params.addValue("startDate", quarterlySeason.getStart());
    params.addValue("endDate", quarterlySeason.getEnd());
    params.addValue("finalized", false);
    params.addValue("quarter", quarterlySeason.getQuarter().getValue());
    params.addValue("numGames", quarterlySeason.getNumGames());
    params.addValue("numGamesPlayed", quarterlySeason.getNumGamesPlayed());
    params.addValue("qTocCollected", quarterlySeason.getQTocCollected());
    params.addValue("qTocPerGame", quarterlySeason.getQTocPerGame());
    params.addValue("numPayouts", quarterlySeason.getNumPayouts());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    //noinspection ConstantConditions
    return keyHolder.getKey().intValue();
  }

  private static final String UPDATE_SQL = "UPDATE quarterlyseason set " +
    "seasonId=:seasonId, startDate=:startDate, endDate=:endDate, " +
    "finalized=:finalized, quarter=:quarter, numGames=:numGames, " +
    "numGamesPlayed=:numGamesPlayed, qTocCollected=:qTocCollected, " +
    "qTocPerGame=:qTocPerGame, numPayouts=:numPayouts, " +
    "lastCalculated=:lastCalculated " +
    " where id=:id";

  @SuppressWarnings("Duplicates")
  public void update(final QuarterlySeason qSeason) {

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", qSeason.getSeasonId());
    params.addValue("startDate", qSeason.getStart());
    params.addValue("endDate", qSeason.getEnd());
    params.addValue("finalized", qSeason.isFinalized());
    params.addValue("quarter", qSeason.getQuarter().getValue());
    params.addValue("numGames", qSeason.getNumGames());
    params.addValue("numGamesPlayed", qSeason.getNumGamesPlayed());
    params.addValue("qTocCollected", qSeason.getQTocCollected());
    params.addValue("qTocPerGame", qSeason.getQTocPerGame());
    params.addValue("numPayouts", qSeason.getNumPayouts());
    params.addValue("lastCalculated", qSeason.getLastCalculated());
    params.addValue("id", qSeason.getId());

    jdbcTemplate.update(UPDATE_SQL, params);
  }

  public QuarterlySeason getById(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    try {
      return jdbcTemplate
        .queryForObject("select * from quarterlyseason where id = :id", params, new QuarterlySeasonMapper());
    } catch (Exception e) {
      return null;
    }
  }

  public List<QuarterlySeason> getBySeasonId(int seasonId) {
    return jdbcTemplate.query("select * from quarterlyseason "
        + " where seasonId=" + seasonId + " order by quarter",
      new QuarterlySeasonMapper());
  }

  public QuarterlySeason getCurrent() {
    MapSqlParameterSource params = new MapSqlParameterSource();
    // This is a bit of a hack. Ideally there would only be one current season. But the tests create multiple quarterly seasons in the same date range. Hence get all the quarterly seasons that encompass the date and take the lastest one (the one with the highest id).

    List<QuarterlySeason> qSeasons = jdbcTemplate.query("select * from quarterlyseason where CURRENT_DATE >= startDate and CURRENT_DATE <= endDate order by id desc", params, new QuarterlySeasonMapper());

    if (qSeasons.size() > 0) {
      return qSeasons.get(0);
    }

    throw new IncorrectResultSizeDataAccessException(0);
  }

  public QuarterlySeason getByDate(LocalDate date) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("date", date);
    List<QuarterlySeason> qSeasons = jdbcTemplate.query("select * from quarterlyseason where :date >= startDate and :date <= endDate order by id desc", params, new QuarterlySeasonMapper());

    if (qSeasons.size() > 0) {
      return qSeasons.get(0);
    }

    throw new IncorrectResultSizeDataAccessException(0);
  }

  private static final class QuarterlySeasonMapper implements RowMapper<QuarterlySeason> {
    @SuppressWarnings("Duplicates")
    public QuarterlySeason mapRow(ResultSet rs, int rowNum) {
      QuarterlySeason quarterly = new QuarterlySeason();
      try {
        quarterly.setId(rs.getInt("id"));
        quarterly.setSeasonId(rs.getInt("seasonId"));
        quarterly.setStart(rs.getDate("startDate").toLocalDate());
        quarterly.setEnd(rs.getDate("endDate").toLocalDate());
        quarterly.setNumGames(rs.getInt("numGames"));
        quarterly.setNumGamesPlayed(rs.getInt("numGamesPlayed"));
        quarterly.setQTocCollected(rs.getInt("qTocCollected"));

        Timestamp lastCalculated = rs.getTimestamp("lastCalculated");
        if (lastCalculated != null) {
          quarterly.setLastCalculated(lastCalculated.toLocalDateTime());
        }

        int quarterlyValue = rs.getInt("quarter");
        quarterly.setQuarter(Quarter.fromInt(quarterlyValue));
      } catch (SQLException e) {
        log.error("Problem mapping repository", e);
      }

      return quarterly;
    }
  }

}
