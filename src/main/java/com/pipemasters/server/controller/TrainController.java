package com.pipemasters.server.controller;

import com.pipemasters.server.dto.TrainDto;
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
    public ResponseEntity<TrainDto> create(@RequestBody TrainDto dto) {
        return new ResponseEntity<>(trainService.save(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainDto> get(@PathVariable Long id) {
        return new ResponseEntity<>(trainService.getById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<TrainDto>> getAll() {
        return new ResponseEntity<>(trainService.getAll(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainDto> update(@PathVariable Long id, @RequestBody TrainDto dto) {
        return new ResponseEntity<>(trainService.update(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/chiefs/unique")
    public ResponseEntity<List<String>> getUniqueChiefs() {
        return ResponseEntity.ok(trainService.getUniqueChiefs());
    }
}