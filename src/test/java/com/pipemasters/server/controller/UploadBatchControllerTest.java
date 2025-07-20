package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.service.UploadBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UploadBatchControllerTest {
    @Mock
    private UploadBatchService uploadBatchService;

    @InjectMocks
    private UploadBatchController uploadBatchController;

    @Test
    void getFiltered_ReturnsOkStatusAndPage() {
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
        UploadBatchDtoSmallResponse dto = new UploadBatchDtoSmallResponse();
        PageDto<UploadBatchDtoSmallResponse> pageDto = new PageDto<>(List.of(dto), 0, 15, 1);

        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), eq(pageable)))
                .thenReturn(pageDto);

        ResponseEntity<Page<UploadBatchDtoSmallResponse>> response = uploadBatchController.getFiltered(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null,
                null,
                null,
                Instant.now(),
                Instant.now(),
                123L,
                10L,
                20L,
                30L,
                Set.of("k1", "k2"),
                pageable
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pageDto.toPage(pageable), response.getBody());
        verify(uploadBatchService).getFilteredBatches(any(UploadBatchFilter.class), eq(pageable));
    }
}