package com.texastoc.repository;

import com.texastoc.model.game.Game;
import com.texastoc.model.season.Quarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
@Slf4j
@Repository
public class GameRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public GameRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String INSERT_SQL =
    "INSERT INTO game "
      + "(seasonId, qSeasonId, gameDate, hostId, hostName, quarter, transportRequired, kittyCost, buyInCost, rebuyAddOnCost, rebuyAddOnTocDebit, annualTocCost, quarterlyTocCost, numPlayers, buyInCollected, rebuyAddOnCollected, annualTocCollected, quarterlyTocCollected, totalCollected, kittyCalculated, annualTocFromRebuyAddOnCalculated, rebuyAddOnLessAnnualTocCalculated, totalCombinedTocCalculated, prizePotCalculated, seasonGameNum, quarterlyGameNum, finalized, lastCalculated, started) "
      + " VALUES "
      + " (:seasonId, :qSeasonId, :gameDate, :hostId, :hostName, :quarter, :transportRequired, :kittyCost, :buyInCost, :rebuyAddOnCost, :rebuyAddOnTocDebit, :annualTocCost, :quarterlyTocCost, :numPlayers, :buyInCollected, :rebuyAddOnCollected, :annualTocCollected, :quarterlyTocCollected, :totalCollected, :kittyCalculated, :annualTocFromRebuyAddOnCalculated, :rebuyAddOnLessAnnualTocCalculated, :totalCombinedTocCalculated, :prizePotCalculated, :seasonGameNum, :quarterlyGameNum, :finalized, :lastCalculated, :started)";

  public int save(final Game game) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", game.getSeasonId());
    params.addValue("qSeasonId", game.getQSeasonId());
    params.addValue("gameDate", game.getDate());
    params.addValue("hostId", game.getHostId());
    params.addValue("hostName", game.getHostName());
    params.addValue("quarter", game.getQuarter().getValue());
    params.addValue("transportRequired", game.isTransportRequired());
    params.addValue("kittyCost", game.getKittyCost());
    params.addValue("buyInCost", game.getBuyInCost());
    params.addValue("rebuyAddOnCost", game.getRebuyAddOnCost());
    params.addValue("rebuyAddOnTocDebit", game.getRebuyAddOnTocDebit());
    params.addValue("annualTocCost", game.getAnnualTocCost());
    params.addValue("quarterlyTocCost", game.getQuarterlyTocCost());
    params.addValue("numPlayers", game.getNumPlayers());
    params.addValue("buyInCollected", game.getBuyInCollected());
    params.addValue("rebuyAddOnCollected", game.getRebuyAddOnCollected());
    params.addValue("annualTocCollected", game.getAnnualTocCollected());
    params.addValue("quarterlyTocCollected", game.getQuarterlyTocCollected());
    params.addValue("totalCollected", game.getKittyCalculated());
    params.addValue("kittyCalculated", game.getKittyCalculated());
    params.addValue("annualTocFromRebuyAddOnCalculated", game.getAnnualTocFromRebuyAddOnCalculated());
    params.addValue("rebuyAddOnLessAnnualTocCalculated", game.getRebuyAddOnLessAnnualTocCalculated());
    params.addValue("totalCombinedTocCalculated", game.getTotalCombinedTocCalculated());
    params.addValue("prizePotCalculated", game.getPrizePotCalculated());
    params.addValue("seasonGameNum", game.getSeasonGameNum());
    params.addValue("quarterlyGameNum", game.getQuarterlyGameNum());
    params.addValue("finalized", game.isFinalized());
    params.addValue("lastCalculated", game.getLastCalculated());
    params.addValue("started", game.getStarted() == null ? null : Timestamp.valueOf(game.getStarted()));

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    //noinspection ConstantConditions
    return keyHolder.getKey().intValue();
  }

  private static final String UPDATE_SQL = "UPDATE game set " +
    "seasonId=:seasonId, qSeasonId=:qSeasonId, hostId=:hostId, " +
    "hostName=:hostName, quarter=:quarter, " +
    "transportRequired=:transportRequired, " +
    "kittyCost=:kittyCost, buyInCost=:buyInCost, rebuyAddOnCost=:rebuyAddOnCost, " +
    "rebuyAddOnTocDebit=:rebuyAddOnTocDebit, annualTocCost=:annualTocCost, " +
    "quarterlyTocCost=:quarterlyTocCost, started=:started, numPlayers=:numPlayers, " +
    "buyInCollected=:buyInCollected, rebuyAddOnCollected=:rebuyAddOnCollected, " +
    "annualTocCollected=:annualTocCollected, " +
    "quarterlyTocCollected=:quarterlyTocCollected, " +
    "totalCollected=:totalCollected, kittyCalculated=:kittyCalculated, " +
    "annualTocFromRebuyAddOnCalculated=:annualTocFromRebuyAddOnCalculated, " +
    "rebuyAddOnLessAnnualTocCalculated=:rebuyAddOnLessAnnualTocCalculated, " +
    "totalCombinedTocCalculated=:totalCombinedTocCalculated, " +
    "prizePotCalculated=:prizePotCalculated, finalized=:finalized, " +
    "payoutDelta=:payoutDelta, lastCalculated=:lastCalculated " +
    " where id=:id";

  public void update(final Game game) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", game.getSeasonId());
    params.addValue("qSeasonId", game.getQSeasonId());
    params.addValue("hostId", game.getHostId());
    params.addValue("hostName", game.getHostName());
    params.addValue("quarter", game.getQuarter().getValue());
    params.addValue("transportRequired", game.isTransportRequired());
    params.addValue("kittyCost", game.getKittyCost());
    params.addValue("buyInCost", game.getBuyInCost());
    params.addValue("rebuyAddOnCost", game.getRebuyAddOnCost());
    params.addValue("rebuyAddOnTocDebit", game.getRebuyAddOnTocDebit());
    params.addValue("annualTocCost", game.getAnnualTocCost());
    params.addValue("quarterlyTocCost", game.getQuarterlyTocCost());
    params.addValue("started", game.getStarted() == null ? null : Timestamp.valueOf(game.getStarted()));
    params.addValue("numPlayers", game.getNumPlayers());
    params.addValue("buyInCollected", game.getBuyInCollected());
    params.addValue("rebuyAddOnCollected", game.getRebuyAddOnCollected());
    params.addValue("annualTocCollected", game.getAnnualTocCollected());
    params.addValue("quarterlyTocCollected", game.getQuarterlyTocCollected());
    params.addValue("totalCollected", game.getTotalCollected());
    params.addValue("kittyCalculated", game.getKittyCalculated());
    params.addValue("annualTocFromRebuyAddOnCalculated", game.getAnnualTocFromRebuyAddOnCalculated());
    params.addValue("rebuyAddOnLessAnnualTocCalculated", game.getRebuyAddOnLessAnnualTocCalculated());
    params.addValue("totalCombinedTocCalculated", game.getTotalCombinedTocCalculated());
    params.addValue("prizePotCalculated", game.getPrizePotCalculated());
    params.addValue("finalized", game.isFinalized());
    params.addValue("payoutDelta", game.getPayoutDelta());
    params.addValue("lastCalculated", game.getLastCalculated());
    params.addValue("id", game.getId());

    jdbcTemplate.update(UPDATE_SQL, params);
  }

  private static final String UPDATE_CAN_REBUY_SQL = "UPDATE game set " +
    "canRebuy=:canRebuy where id=:id";


  @CacheEvict(value = "currentGame", allEntries = true)
  @Transactional
  public void updateCanRebuy(final boolean canRebuy, int gameId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("canRebuy", canRebuy);
    params.addValue("id", gameId);
    jdbcTemplate.update(UPDATE_CAN_REBUY_SQL, params);
  }


  public Game getById(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);
    return jdbcTemplate
      .queryForObject("select * from game where id = :id", params, new GameMapper());
  }

  public List<Game> getBySeasonId(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);

    List<Game> games = Collections.emptyList();
    try {
      games = jdbcTemplate
        .query("select * from game where seasonId = :seasonId order by gameDate desc", params, new GameMapper());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return games;
  }

  public List<Game> getByQuarterlySeasonId(int qSeasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("qSeasonId", qSeasonId);

    List<Game> games = Collections.emptyList();
    try {
      games = jdbcTemplate
        .query("select * from game where qSeasonId = :qSeasonId", params, new GameMapper());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return games;
  }

  public List<Game> getUnfinalized(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    return jdbcTemplate
      .query("select * from game where seasonId = :seasonId and finalized = false", params, new GameMapper());
  }

  public List<Game> getMostRecent(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    return jdbcTemplate
      .query("select * from game where seasonId = :seasonId order by gameDate desc limit 1", params, new GameMapper());
  }

  private static final class GameMapper implements RowMapper<Game> {
    @Override
    public Game mapRow(ResultSet rs, int rowNum) {
      Game game = new Game();
      try {
        game.setId(rs.getInt("id"));
        game.setSeasonId(rs.getInt("seasonId"));
        game.setQSeasonId(rs.getInt("qSeasonId"));
        game.setDate(rs.getDate("gameDate").toLocalDate());
        game.setQuarter(Quarter.fromInt(rs.getInt("quarter")));
        game.setTransportRequired(rs.getBoolean("transportRequired"));
        game.setKittyCost(rs.getInt("kittyCost"));
        game.setBuyInCost(rs.getInt("buyInCost"));
        game.setRebuyAddOnCost(rs.getInt("rebuyAddOnCost"));
        game.setRebuyAddOnTocDebit(rs.getInt("rebuyAddOnTocDebit"));
        game.setAnnualTocCost(rs.getInt("annualTocCost"));
        game.setQuarterlyTocCost(rs.getInt("quarterlyTocCost"));
        game.setNumPlayers(rs.getInt("numPlayers"));
        game.setBuyInCollected(rs.getInt("buyInCollected"));
        game.setRebuyAddOnCollected(rs.getInt("rebuyAddOnCollected"));
        game.setAnnualTocCollected(rs.getInt("annualTocCollected"));
        game.setQuarterlyTocCollected(rs.getInt("quarterlyTocCollected"));
        game.setTotalCollected(rs.getInt("totalCollected"));
        game.setKittyCalculated(rs.getInt("kittyCalculated"));
        game.setAnnualTocFromRebuyAddOnCalculated(rs.getInt("annualTocFromRebuyAddOnCalculated"));
        game.setRebuyAddOnLessAnnualTocCalculated(rs.getInt("rebuyAddOnLessAnnualTocCalculated"));
        game.setTotalCombinedTocCalculated(rs.getInt("totalCombinedTocCalculated"));
        game.setPrizePotCalculated(rs.getInt("prizePotCalculated"));
        game.setFinalized(rs.getBoolean("finalized"));
        game.setHostId(rs.getInt("hostId"));
        game.setPayoutDelta(rs.getInt("payoutDelta"));
        game.setSeasonGameNum(rs.getInt("seasonGameNum"));
        game.setQuarterlyGameNum(rs.getInt("quarterlyGameNum"));
        game.setCanRebuy(rs.getBoolean("canRebuy"));

        String value = rs.getString("hostName");
        if (value != null) {
          game.setHostName(rs.getString("hostName"));
        }

        Timestamp time = rs.getTimestamp("started");
        if (time != null) {
          game.setStarted(time.toLocalDateTime());
        }

        time = rs.getTimestamp("lastCalculated");
        if (time != null) {
          game.setLastCalculated(time.toLocalDateTime());
        }

      } catch (SQLException e) {
        log.error("Problem mapping game", e);
      }

      return game;
    }
  }

}
