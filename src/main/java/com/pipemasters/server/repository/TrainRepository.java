package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainRepository extends GeneralRepository<Train, Long> {
    void deleteById(Long id);
    @Query("SELECT DISTINCT t.chief FROM Train t")
    List<User> findDistinctChiefs();
    boolean existsByTrainNumber(Long trainNumber);
    List<Train> findByBranchId(Long branchId);
    @Query("SELECT DISTINCT t.chief FROM Train t WHERE t.branch.id = :branchId AND t.chief IS NOT NULL")
    List<User> findDistinctChiefsByBranchId(Long branchId);
}
