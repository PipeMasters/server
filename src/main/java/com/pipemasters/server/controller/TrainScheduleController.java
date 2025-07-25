package com.pipemasters.server.controller;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.service.TrainScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

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
}