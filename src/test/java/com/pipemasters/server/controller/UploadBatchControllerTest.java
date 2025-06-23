package com.pipemasters.server.controller;

import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.service.UploadBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadBatchControllerTest {

    @Mock
    private UploadBatchService uploadBatchService;

    @InjectMocks
    private UploadBatchController uploadBatchController;

    @Test
    void getFiltered_ReturnsOkStatusAndPage() {
        // given
        UploadBatchDto dto1 = new UploadBatchDto();
        UploadBatchDto dto2 = new UploadBatchDto();
        List<UploadBatchDto> dtos = List.of(dto1, dto2);
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UploadBatchDto> page = new PageImpl<>(dtos, pageable, dtos.size());

        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class)))
                .thenReturn(page);

        Set<String> keywords = Set.of("ключевое", "видео", "путь");

        // when
        ResponseEntity<Page<UploadBatchDto>> response = uploadBatchController.getFiltered(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null,
                "123",
                "Иванов",
                "Петров",
                keywords,
                pageable
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page, response.getBody());
        verify(uploadBatchService).getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class));
    }
}