package com.pipemasters.server.service;

import com.pipemasters.server.dto.TrainDTO;
import java.util.List;

public interface TrainService {
    TrainDTO save(TrainDTO trainDto);
    TrainDTO getById(Long id);
    List<TrainDTO> getAll();
    TrainDTO update(Long id, TrainDTO trainDto);
    void delete(Long id);
}