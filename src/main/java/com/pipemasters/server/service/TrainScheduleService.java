package com.pipemasters.server.service;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.create.TrainScheduleCreateDto;
import com.pipemasters.server.dto.request.update.TrainScheduleUpdateDto;
import com.pipemasters.server.dto.response.TrainScheduleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TrainScheduleService {
    ParsingStatsDto parseExcelFile(MultipartFile file) throws IOException;
    TrainScheduleResponseDto create(TrainScheduleCreateDto requestDto);
    PageDto<TrainScheduleResponseDto> getAllPaginated(Pageable pageable);
    TrainScheduleResponseDto getById(Long id);
    TrainScheduleResponseDto update(Long id, TrainScheduleUpdateDto updateDto);
    void delete(Long id);
}