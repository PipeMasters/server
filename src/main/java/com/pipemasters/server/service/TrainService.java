package com.pipemasters.server.service;

import com.pipemasters.server.dto.TrainDto;

import java.util.List;

public interface TrainService {
    TrainDto save(TrainDto trainDto);

    TrainDto getById(Long id);

    List<TrainDto> getAll();

    TrainDto update(Long id, TrainDto trainDto);

    void delete(Long id);
}