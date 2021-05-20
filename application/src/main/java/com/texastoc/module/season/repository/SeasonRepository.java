package com.texastoc.module.season.repository;

import com.texastoc.module.season.model.Season;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends CrudRepository<Season, Integer> {

  @Query("select * from season where finalized = false order by id desc")
  List<Season> findUnfinalized();

  @Query("select * from season order by id desc limit 1")
  List<Season> findMostRecent();

}
