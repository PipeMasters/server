package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.service.UploadBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UploadBatchControllerTest {
//
//    @Mock
//    private UploadBatchService uploadBatchService;
//
//    @InjectMocks
//    private UploadBatchController uploadBatchController;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    void setup() {
//        mockMvc = MockMvcBuilders.standaloneSetup(uploadBatchController)
//                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//                .build();
//    }
//
//    @Test
//    void getFiltered_ReturnsOkStatusAndPage() {
//        UploadBatchResponseDto dto1 = new UploadBatchResponseDto();
//        dto1.setDirectory(UUID.randomUUID().toString());
//        dto1.setUploadedId(1L);
//        dto1.setCreatedAt(Instant.now());
//        dto1.setTrainDeparted(LocalDate.of(2024, 1, 1));
//        dto1.setTrainId(101L);
//        dto1.setBranchId(201L);
//
//        UploadBatchResponseDto dto2 = new UploadBatchResponseDto();
//        dto2.setDirectory(UUID.randomUUID().toString());
//        dto2.setUploadedId(2L);
//        dto2.setCreatedAt(Instant.now());
//        dto2.setTrainDeparted(LocalDate.of(2024, 1, 2));
//        dto2.setTrainId(102L);
//        dto2.setBranchId(202L);
//
//        List<UploadBatchResponseDto> dtos = List.of(dto1, dto2);
//        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
//        PageDto<UploadBatchResponseDto> pageDto = new PageDto<>(dtos, pageable.getPageNumber(), pageable.getPageSize(), dtos.size());
//
//
//        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class)))
//                .thenReturn(pageDto);
//
//        Set<String> keywords = Set.of("ключевое", "видео", "путь");
//
//        ResponseEntity<Page<UploadBatchResponseDto>> response = uploadBatchController.getFiltered(
//                LocalDate.of(2024, 1, 1),
//                LocalDate.of(2024, 12, 31),
//                null,
//                "123",
//                "Иванов",
//                "Петров",
//                keywords,
//                pageable
//        );
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        Page<UploadBatchResponseDto> expectedPage = pageDto.toPage(pageable);
//        assertEquals(expectedPage, response.getBody());
//        verify(uploadBatchService).getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class));
//    }
//
//    @Test
//    void getFiltered_shouldNotCauseRecursionInJsonSerialization() throws Exception {
//        UploadBatchResponseDto recursiveDto = new UploadBatchResponseDto();
//        recursiveDto.setDirectory(UUID.randomUUID().toString());
//        recursiveDto.setUploadedId(1L);
//        recursiveDto.setCreatedAt(Instant.now());
//        recursiveDto.setTrainDeparted(LocalDate.of(2024, 1, 1));
//        recursiveDto.setTrainId(101L);
//        recursiveDto.setBranchId(201L);
//
//        List<UploadBatchResponseDto> dtos = List.of(recursiveDto);
//        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
//        PageDto<UploadBatchResponseDto> pageDto = new PageDto<>(dtos, pageable.getPageNumber(), pageable.getPageSize(), dtos.size());
//
//        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class)))
//                .thenReturn(pageDto);
//
//        mockMvc.perform(get("/api/v1/batch")
//                        .param("dateFrom", "2024-01-01")
//                        .param("dateTo", "2024-12-31")
//                        .param("trainNumber", "123")
//                        .param("chiefName", "Иванов")
//                        .param("uploadedByName", "Петров")
//                        .param("keywords", "ключевое", "видео", "путь")
//                        .param("page", "0")
//                        .param("size", "15")
//                        .param("sort", "createdAt,desc")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        verify(uploadBatchService).getFilteredBatches(any(UploadBatchFilter.class), any(Pageable.class));
//    }
}