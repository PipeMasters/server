package com.pipemasters.server.service;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TrainService {
    TrainResponseDto save(TrainRequestDto trainRequestDto);
    TrainResponseDto getById(Long id);
    List<TrainResponseDto> getAll();
    PageDto<TrainResponseDto> getPaginatedTrains(Pageable pageable);
    TrainResponseDto update(Long id, TrainRequestDto trainRequestDto);
    void delete(Long id);
    List<UserResponseDto> getChiefs();
    TrainResponseDto assignTrainToBranch(Long trainId, Long branchId);
    TrainResponseDto updateTrainChief(Long trainId, Long newChiefId);
    List<TrainResponseDto> getTrainsByBranchId(Long branchId);
    List<UserResponseDto> getChiefsByBranchId(Long branchId);
}