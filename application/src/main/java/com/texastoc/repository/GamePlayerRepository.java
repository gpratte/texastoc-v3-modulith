package com.texastoc.repository;

import com.texastoc.model.game.GamePlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
@Slf4j
@Repository
public class GamePlayerRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public GamePlayerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  public List<GamePlayer> selectByGameId(int gameId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", gameId);
    List<GamePlayer> gamePlayers = jdbcTemplate
      .query("select * from gameplayer"
          + " where gameId = :gameId ",
        params,
        new GamePlayerMapper());
    Collections.sort(gamePlayers);
    return gamePlayers;
  }

  public List<GamePlayer> selectAnnualTocPlayersBySeasonId(int seasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("seasonId", seasonId);
    List<GamePlayer> gamePlayers = jdbcTemplate
      .query("select * from gameplayer "
          + " where seasonId = :seasonId "
          + " and annualTocCollected IS NOT NULL ",
        params,
        new GamePlayerMapper());
    Collections.sort(gamePlayers);
    return gamePlayers;
  }

  public List<GamePlayer> selectQuarterlyTocPlayersByQuarterlySeasonId(int qSeasonId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("qSeasonId", qSeasonId);
    List<GamePlayer> gamePlayers = jdbcTemplate
      .query("select * from gameplayer "
          + " where qSeasonId = :qSeasonId "
          + " and quarterlyTocCollected IS NOT NULL ",
        params,
        new GamePlayerMapper());
    Collections.sort(gamePlayers);
    return gamePlayers;
  }

  public GamePlayer selectById(int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);

    GamePlayer gamePlayer;
    try {
      gamePlayer = jdbcTemplate
        .queryForObject("select * from gameplayer where id = :id", params, new GamePlayerMapper());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return gamePlayer;
  }

  public void deleteById(int gameId, int id) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("gameId", gameId);
    params.addValue("id", id);

    jdbcTemplate
      .update("delete from gameplayer where gameId = :gameId and id = :id", params);
  }

  private static final String INSERT_SQL =
    "INSERT INTO gameplayer "
      + "(playerId, gameId, qSeasonId, seasonId, name, points, place, knockedOut, roundUpdates, buyInCollected, rebuyAddOnCollected, annualTocCollected, quarterlyTocCollected, chop) "
      + " VALUES "
      + " (:playerId, :gameId, :qSeasonId, :seasonId, :name, :points, :place, :knockedOut, :roundUpdates, :buyInCollected, :rebuyAddOnCollected, :annualTocCollected, :quarterlyTocCollected, :chop)";

  public int save(final GamePlayer gamePlayer) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("playerId", gamePlayer.getPlayerId());
    params.addValue("gameId", gamePlayer.getGameId());
    params.addValue("qSeasonId", gamePlayer.getQSeasonId());
    params.addValue("seasonId", gamePlayer.getSeasonId());
    params.addValue("name", gamePlayer.getName());
    params.addValue("points", gamePlayer.getPoints());
    params.addValue("place", gamePlayer.getPlace());
    params.addValue("knockedOut", gamePlayer.getKnockedOut());
    params.addValue("roundUpdates", gamePlayer.getRoundUpdates());
    params.addValue("buyInCollected", gamePlayer.getBuyInCollected());
    params.addValue("rebuyAddOnCollected", gamePlayer.getRebuyAddOnCollected());
    params.addValue("annualTocCollected", gamePlayer.getAnnualTocCollected());
    params.addValue("quarterlyTocCollected", gamePlayer.getQuarterlyTocCollected());
    params.addValue("knockedOut", gamePlayer.getKnockedOut());
    params.addValue("chop", gamePlayer.getChop());

    String[] keys = {"id"};
    jdbcTemplate.update(INSERT_SQL, params, keyHolder, keys);

    //noinspection ConstantConditions
    return keyHolder.getKey().intValue();
  }


  private static final String UPDATE_SQL = "UPDATE gameplayer set " +
    "playerId=:playerId, gameId=:gameId, qSeasonId=:qSeasonId, seasonId=seasonId, " +
    "name=:name, points=:points, place=:place, knockedOut=:knockedOut, " +
    "roundUpdates=:roundUpdates, buyInCollected=:buyInCollected, " +
    "rebuyAddOnCollected=:rebuyAddOnCollected, annualTocCollected=:annualTocCollected, " +
    "quarterlyTocCollected=:quarterlyTocCollected, chop=:chop " +
    " where id=:id";

  public void update(final GamePlayer player) {

    // Places are 1 through 10
    Integer place = player.getPlace();
    if (place != null && place > 10) {
      place = null;
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("playerId", player.getPlayerId());
    params.addValue("gameId", player.getGameId());
    params.addValue("qSeasonId", player.getQSeasonId());
    params.addValue("seasonId", player.getSeasonId());
    params.addValue("name", player.getName());
    params.addValue("points", player.getPoints());
    params.addValue("place", place);
    params.addValue("knockedOut", player.getKnockedOut());
    params.addValue("roundUpdates", player.getRoundUpdates());
    params.addValue("buyInCollected", player.getBuyInCollected());
    params.addValue("rebuyAddOnCollected", player.getRebuyAddOnCollected());
    params.addValue("annualTocCollected", player.getAnnualTocCollected());
    params.addValue("quarterlyTocCollected", player.getQuarterlyTocCollected());
    params.addValue("chop", player.getChop());

    params.addValue("id", player.getId());

    jdbcTemplate.update(UPDATE_SQL, params);
  }

  public Integer getNumGamesByPlayerId(int playerId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("playerId", playerId);
    return jdbcTemplate
      .queryForObject("select count(*) from gameplayer where playerId = :playerId", params, Integer.class);
  }


  private static final class GamePlayerMapper implements RowMapper<GamePlayer> {
    @Override
    public GamePlayer mapRow(ResultSet rs, int rowNum) {
      GamePlayer gamePlayer = new GamePlayer();
      try {
        gamePlayer.setId(rs.getInt("id"));
        gamePlayer.setPlayerId(rs.getInt("playerId"));
        gamePlayer.setGameId(rs.getInt("gameId"));
        gamePlayer.setQSeasonId(rs.getInt("qSeasonId"));
        gamePlayer.setSeasonId(rs.getInt("seasonId"));
        gamePlayer.setName(rs.getString("name"));

        String value = rs.getString("buyInCollected");
        if (value != null) {
          gamePlayer.setBuyInCollected(Integer.parseInt(value));
        }

        value = rs.getString("rebuyAddOnCollected");
        if (value != null) {
          gamePlayer.setRebuyAddOnCollected(Integer.parseInt(value));
        }

        value = rs.getString("chop");
        if (value != null) {
          gamePlayer.setChop(Integer.parseInt(value));
        }

        value = rs.getString("points");
        if (value != null) {
          gamePlayer.setPoints(Integer.parseInt(value));
        }

        value = rs.getString("place");
        if (value != null) {
          gamePlayer.setPlace(Integer.parseInt(value));
        }

        value = rs.getString("annualTocCollected");
        if (value != null) {
          gamePlayer.setAnnualTocCollected(Integer.parseInt(value));
        }

        value = rs.getString("quarterlyTocCollected");
        if (value != null) {
          gamePlayer.setQuarterlyTocCollected(Integer.parseInt(value));
        }

        value = rs.getString("quarterlyTocCollected");
        if (value != null) {
          gamePlayer.setQuarterlyTocCollected(Integer.parseInt(value));
        }

        value = rs.getString("knockedOut");
        if (value != null) {
          gamePlayer.setKnockedOut(rs.getBoolean("knockedOut"));
        }

        value = rs.getString("roundUpdates");
        if (value != null) {
          gamePlayer.setRoundUpdates(rs.getBoolean("roundUpdates"));
        }

      } catch (SQLException e) {
        log.error("problem mapping game player", e);
      }

      return gamePlayer;
    }
  }

}
