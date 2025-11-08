package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.create.TrainScheduleCreateDto;
import com.pipemasters.server.dto.request.update.TrainScheduleUpdateDto;
import com.pipemasters.server.dto.response.TrainScheduleResponseDto;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.service.TrainScheduleService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/train-schedules")
public class TrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public TrainScheduleController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
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

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        ByteArrayOutputStream out = trainScheduleService.exportSchedulesToExcel();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "train_schedules_" + timestamp + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}