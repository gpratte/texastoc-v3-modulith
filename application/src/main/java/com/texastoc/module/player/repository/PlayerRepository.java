package com.texastoc.module.player.repository;

import com.texastoc.module.player.model.Player;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {
  String USER = "USER";

  @Query("select * from player where email=:email")
  List<Player> findByEmail(@Param("email") String email);
}
