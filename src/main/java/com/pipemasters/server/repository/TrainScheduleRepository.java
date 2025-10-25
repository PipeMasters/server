package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TrainSchedule;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainScheduleRepository extends GeneralRepository<TrainSchedule, Long> {
    Optional<TrainSchedule> findByTrainNumber(String trainNumber);
    List<TrainSchedule> findByTrainNumberIn(Collection<String> trainNumbers);
    void deleteById(Long id);
}
