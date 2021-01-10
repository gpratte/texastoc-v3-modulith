package com.texastoc.module.game.repository;

import com.texastoc.module.game.model.Game;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<Game, Integer> {
  @Query("select * from game where season_id=:seasonId")
  List<Game> findBySeasonId(@Param("seasonId") int seasonId);

  @Query("select * from game where q_season_id=:qSeasonId")
  List<Game> findByQuarterlySeasonId(@Param("qSeasonId") int qSeasonId);

  @Query("select * from game where season_id = :seasonId and finalized = false")
  List<Game> findUnfinalizedBySeasonId(@Param("seasonId") int seasonId);

  @Query("select * from game where season_id = :seasonId order by gameDate desc limit 1")
  List<Game> findMostRecentBySeasonId(@Param("seasonId") int seasonId);
}
