package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.service.UploadBatchService;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/batch")
public class UploadBatchController {
    private final UploadBatchService uploadBatchService;

    public UploadBatchController(UploadBatchService uploadBatchService) {
        this.uploadBatchService = uploadBatchService;
    }

    @GetMapping
    public ResponseEntity<Page<UploadBatchDtoSmallResponse>> getFiltered(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDateTo,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant createdTo,

            @RequestParam(required = false) Long trainId,
            @RequestParam(required = false) String chief,
            @RequestParam(required = false) Long uploadedById,
            @RequestParam(required = false) Long branchId,

            @RequestParam(required = false) Set<String> keywords,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UploadBatchFilter filter = new UploadBatchFilter();
        filter.setDepartureDateFrom(departureDateFrom);
        filter.setDepartureDateTo(departureDateTo);

        filter.setSpecificDate(specificDate);

        filter.setArrivalDateFrom(arrivalDateFrom);
        filter.setArrivalDateTo(arrivalDateTo);

        filter.setCreatedFrom(createdFrom);
        filter.setCreatedTo(createdTo);

        filter.setTrainId(trainId);
        filter.setChiefName(chief);

        filter.setUploadedById(uploadedById);

        filter.setBranchId(branchId);

        filter.setKeywords(keywords);

        PageDto<UploadBatchDtoSmallResponse> dtoPage = uploadBatchService.getFilteredBatches(filter, pageable);
        return new ResponseEntity<>(dtoPage.toPage(pageable), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UploadBatchResponseDto> create(@RequestBody UploadBatchRequestDto uploadBatchRequestDto) {
        return new ResponseEntity<>(uploadBatchService.save(uploadBatchRequestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadBatchResponseDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(uploadBatchService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UploadBatchDtoSmallResponse>> getAll() {
        return new ResponseEntity<>(uploadBatchService.getAll(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UploadBatchResponseDto> update(@PathVariable Long id, @RequestBody UploadBatchRequestDto dto) {
        return new ResponseEntity<>(uploadBatchService.update(id, dto), HttpStatus.OK);
    }
}