package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.service.TrainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/train")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }
    @PostMapping
    public ResponseEntity<TrainResponseDto> create(@RequestBody TrainRequestDto dto) {
        return new ResponseEntity<>(trainService.save(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainResponseDto> get(@PathVariable Long id) {
        return new ResponseEntity<>(trainService.getById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<TrainResponseDto>> getAll() {
        return new ResponseEntity<>(trainService.getAll(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainResponseDto> update(@PathVariable Long id, @RequestBody TrainRequestDto dto) {
        return new ResponseEntity<>(trainService.update(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chiefs")
    public ResponseEntity<List<UserResponseDto>> getChiefs() {
        return ResponseEntity.ok(trainService.getChiefs());
    }
}