package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.service.UploadBatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<UploadBatchResponseDto>> getFiltered(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) String trainNumber,
            @RequestParam(required = false) String chief,
            @RequestParam(required = false) String uploadedByName,
            @RequestParam(required = false) Set<String> keywords,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UploadBatchFilter filter = new UploadBatchFilter();
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setSpecificDate(specificDate);
        filter.setTrainNumber(trainNumber);
        filter.setChiefName(chief);
        filter.setUploadedByName(uploadedByName);
        filter.setKeywords(keywords);

        PageDto<UploadBatchResponseDto> dtoPage = uploadBatchService.getFilteredBatches(filter, pageable);
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
    public ResponseEntity<List<UploadBatchResponseDto>> getAll() {
        return new ResponseEntity<>(uploadBatchService.getAll(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UploadBatchResponseDto> update(@PathVariable Long id, @RequestBody UploadBatchRequestDto dto) {
        return new ResponseEntity<>(uploadBatchService.update(id, dto), HttpStatus.OK);
    }
}