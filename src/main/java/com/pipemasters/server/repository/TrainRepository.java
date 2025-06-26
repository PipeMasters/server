package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Train;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainRepository extends GeneralRepository<Train, Long> {
    void deleteById(Long id);
    @Query("SELECT DISTINCT t.chief FROM Train t")
    List<String> findDistinctChiefs();
}
