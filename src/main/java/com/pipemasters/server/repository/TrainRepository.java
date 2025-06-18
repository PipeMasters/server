package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Train;

public interface TrainRepository extends GeneralRepository<Train, Long> {
    void deleteById(Long id);
}
