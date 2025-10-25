package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.service.TrainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/all")
    public ResponseEntity<List<TrainResponseDto>> getAll() {
        return new ResponseEntity<>(trainService.getAll(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TrainResponseDto>> getPaginatedTrains(
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageDto<TrainResponseDto> dtoPage = trainService.getPaginatedTrains(pageable);
        return new ResponseEntity<>(dtoPage.toPage(pageable), HttpStatus.OK);
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

    @PutMapping("/{trainId}/assignBranch/{branchId}")
    public ResponseEntity<TrainResponseDto> assignTrainToBranch(@PathVariable Long trainId, @PathVariable Long branchId) {
        return ResponseEntity.ok(trainService.assignTrainToBranch(trainId, branchId));
    }

    @PutMapping("/{trainId}/chief/{newChiefId}")
    public ResponseEntity<TrainResponseDto> updateTrainChief(@PathVariable Long trainId, @PathVariable Long newChiefId) {
        return ResponseEntity.ok(trainService.updateTrainChief(trainId, newChiefId));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<List<TrainResponseDto>> getTrainsByBranchId(@PathVariable Long branchId) {
        return new ResponseEntity<>(trainService.getTrainsByBranchId(branchId), HttpStatus.OK);
    }

    @GetMapping("/chiefs/by-branch/{branchId}")
    public ResponseEntity<List<UserResponseDto>> getChiefsByBranchId(@PathVariable Long branchId) {
        return new ResponseEntity<>(trainService.getChiefsByBranchId(branchId), HttpStatus.OK);
    }
}