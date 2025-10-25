package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.create.TrainScheduleCreateDto;
import com.pipemasters.server.dto.request.update.TrainScheduleUpdateDto;
import com.pipemasters.server.dto.response.TrainScheduleResponseDto;
import com.pipemasters.server.service.TrainScheduleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/train-schedules")
public class TrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public TrainScheduleController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
    }

    @PostMapping(value = "/upload/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsingStatsDto> uploadExcelFile(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return new ResponseEntity<>(
                    new ParsingStatsDto(
                            0,
                            0,
                            0,
                            0,
                            0,
                            Collections.singletonList("The file to download is missing or empty.")
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TrainScheduleResponseDto> create(@Valid @RequestBody TrainScheduleCreateDto requestDto) {
        TrainScheduleResponseDto createdSchedule = trainScheduleService.create(requestDto);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TrainScheduleResponseDto>> getAllPaginated(
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageDto<TrainScheduleResponseDto> dtoPage = trainScheduleService.getAllPaginated(pageable);
        return new ResponseEntity<>(dtoPage.toPage(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainScheduleResponseDto> getById(@PathVariable Long id) {
        TrainScheduleResponseDto schedule = trainScheduleService.getById(id);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainScheduleResponseDto> update(@PathVariable Long id, @Valid @RequestBody TrainScheduleUpdateDto updateDto) {
        TrainScheduleResponseDto updatedSchedule = trainScheduleService.update(id, updateDto);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}