package com.texastoc.repository;

import com.texastoc.model.season.HistoricalSeason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class SeasonHistoryRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public SeasonHistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String INSERT_SQL = "INSERT INTO historicalseason "
    + " (seasonId, startYear, endYear) "
    + " VALUES "
    + " (:seasonId, :startYear, :endYear)";

  public int save(int seasonId, LocalDate start, LocalDate end) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    params.addValue("startYear", Integer.toString(start.getYear()));
    params.addValue("endYear", Integer.toString(end.getYear()));

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    return keyHolder.getKey().intValue();
  }

  private static final String INSERT_PLAYER_SQL = "INSERT INTO historicalseasonplayer "
    + " (seasonId, name, points, entries) "
    + " VALUES "
    + " (:seasonId, :name, :points, :entries)";

  public int savePlayer(int seasonId, String name, Integer points, Integer entries) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    params.addValue("name", name);
    params.addValue("points", points);
    params.addValue("entries", entries);

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_PLAYER_SQL, params, keyHolder, keys);

    return keyHolder.getKey().intValue();
  }


  public List<HistoricalSeason> getAll() {
    List<HistoricalSeason> historicalSeasons = null;
    MapSqlParameterSource params = new MapSqlParameterSource();
    try {
      historicalSeasons = jdbcTemplate.query("select * from historicalseason", params, new HistoricalSeasonMapper());
    } catch (Exception e) {
      return Collections.emptyList();
    }
    return historicalSeasons;
  }

  public List<HistoricalSeason.HistoricalSeasonPlayer> getAllPlayers(int seasonId) {
    List<HistoricalSeason.HistoricalSeasonPlayer> historicalSeasonPlayers = null;
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    try {
      historicalSeasonPlayers = jdbcTemplate.query("select * from historicalseasonplayer", params, new HistoricalSeasonPlayerMapper());
    } catch (Exception e) {
      return Collections.emptyList();
    }
    return historicalSeasonPlayers;
  }

  public void deleteById(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    jdbcTemplate
      .update("delete from historicalseason where seasonId = :seasonId", params);
  }

  public void deletePlayersById(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    jdbcTemplate
      .update("delete from historicalseasonplayer where seasonId = :seasonId", params);
  }

  private static final class HistoricalSeasonMapper implements RowMapper<HistoricalSeason> {
    @Override
    public HistoricalSeason mapRow(ResultSet rs, int rowNum) {
      HistoricalSeason hs = new HistoricalSeason();
      try {
        hs.setId(rs.getInt("id"));
        hs.setSeasonId(rs.getInt("seasonId"));
        hs.setStartYear(rs.getString("startYear"));
        hs.setEndYear(rs.getString("endYear"));
      } catch (SQLException e) {
        log.error("Error mapping table to object", e);
      }
      return hs;
    }
  }

  private static final class HistoricalSeasonPlayerMapper implements RowMapper<HistoricalSeason.HistoricalSeasonPlayer> {
    @Override
    public HistoricalSeason.HistoricalSeasonPlayer mapRow(ResultSet rs, int rowNum) {
      HistoricalSeason.HistoricalSeasonPlayer hsp = new HistoricalSeason.HistoricalSeasonPlayer();
      try {
        hsp.setName(rs.getString("name"));
        hsp.setPoints(rs.getInt("points"));
        hsp.setEntries(rs.getInt("entries"));
      } catch (SQLException e) {
        log.error("Error mapping table to object", e);
      }
      return hsp;
    }
  }

}
