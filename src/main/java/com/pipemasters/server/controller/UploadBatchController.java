package com.pipemasters.server.controller;

import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.service.UploadBatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/batch")
public class UploadBatchController {
    private final UploadBatchService uploadBatchService;

    public UploadBatchController(UploadBatchService uploadBatchService) {
        this.uploadBatchService = uploadBatchService;
    }

    @GetMapping
    public ResponseEntity<Page<UploadBatchDto>> getFiltered(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate,
            @RequestParam(required = false) String trainNumber,
            @RequestParam(required = false) String chiefName,
            @RequestParam(required = false) String uploadedByName,
            @RequestParam(required = false) Set<String> keywords,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UploadBatchFilter filter = new UploadBatchFilter();
        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setSpecificDate(specificDate);
        filter.setTrainNumber(trainNumber);
        filter.setChiefName(chiefName);
        filter.setUploadedByName(uploadedByName);
        filter.setKeywords(keywords);

        return new ResponseEntity<>(uploadBatchService.getFilteredBatches(filter, pageable), HttpStatus.OK);
    }
}