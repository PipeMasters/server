package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;

import java.util.List;

public interface TrainService {
    TrainResponseDto save(TrainRequestDto trainRequestDto);
    TrainResponseDto getById(Long id);
    List<TrainResponseDto> getAll();
    TrainResponseDto update(Long id, TrainRequestDto trainRequestDto);
    void delete(Long id);
    List<UserResponseDto> getChiefs();
}