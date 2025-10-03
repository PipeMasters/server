package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDtoMediumResponse;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.service.UploadBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UploadBatchControllerTest {
    @Mock
    private UploadBatchService uploadBatchService;

    @InjectMocks
    private UploadBatchController uploadBatchController;

    @Test
    void getFiltered_WithAllParams_ReturnsOkStatusAndPage() {
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));

        UploadBatchDtoMediumResponse dto = new UploadBatchDtoMediumResponse();
        PageDto<UploadBatchDtoMediumResponse> pageDto = new PageDto<>(List.of(dto), 0, 15, 1);

        LocalDate departureDateFrom = LocalDate.of(2024, 1, 1);
        LocalDate departureDateTo = LocalDate.of(2024, 1, 31);
        LocalDate specificDate = LocalDate.of(2024, 1, 15);
        LocalDate arrivalDateFrom = LocalDate.of(2024, 2, 1);
        LocalDate arrivalDateTo = LocalDate.of(2024, 2, 29);
        Instant createdFrom = Instant.parse("2023-01-01T00:00:00Z");
        Instant createdTo = Instant.parse("2023-12-31T23:59:59Z");
        Long trainId = 123L;
        Long chiefId = 10L;
        Long uploadedById = 20L;
        Long branchId = 30L;
        Set<Long> tagId = Set.of(1L, 2L);
        Long id = 500L;
        String comment = "Test comment";
        Boolean archived = true;
        Boolean deleted = false;
        AbsenceCause absenceCause = AbsenceCause.HUMAN_FACTOR;

        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), eq(pageable)))
                .thenReturn(pageDto);

        ResponseEntity<Page<UploadBatchDtoMediumResponse>> response = uploadBatchController.getFiltered(
                departureDateFrom,
                departureDateTo,
                specificDate,
                arrivalDateFrom,
                arrivalDateTo,
                createdFrom,
                createdTo,
                trainId,
                chiefId,
                uploadedById,
                branchId,
                tagId,
                id,
                comment,
                archived,
                deleted,
                absenceCause,
                pageable
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pageDto.toPage(pageable), response.getBody());


        ArgumentCaptor<UploadBatchFilter> filterCaptor = ArgumentCaptor.forClass(UploadBatchFilter.class);

        verify(uploadBatchService).getFilteredBatches(filterCaptor.capture(), eq(pageable));

        UploadBatchFilter capturedFilter = filterCaptor.getValue();

        assertEquals(departureDateFrom, capturedFilter.getDepartureDateFrom());
        assertEquals(departureDateTo, capturedFilter.getDepartureDateTo());
        assertEquals(specificDate, capturedFilter.getSpecificDate());
        assertEquals(arrivalDateFrom, capturedFilter.getArrivalDateFrom());
        assertEquals(arrivalDateTo, capturedFilter.getArrivalDateTo());
        assertEquals(createdFrom, capturedFilter.getCreatedFrom());
        assertEquals(createdTo, capturedFilter.getCreatedTo());
        assertEquals(trainId, capturedFilter.getTrainId());
        assertEquals(chiefId, capturedFilter.getChiefId());
        assertEquals(uploadedById, capturedFilter.getUploadedById());
        assertEquals(branchId, capturedFilter.getBranchId());
        assertEquals(tagId, capturedFilter.getTagIds());
        assertEquals(id, capturedFilter.getId());
        assertEquals(comment, capturedFilter.getComment());
        assertEquals(archived, capturedFilter.getArchived());
        assertEquals(deleted, capturedFilter.getDeleted());
        assertEquals(absenceCause, capturedFilter.getAbsenceCause());
    }

    @Test
    void getFiltered_WithNoParams_ReturnsOkStatusAndPage() {
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
        UploadBatchDtoMediumResponse dto = new UploadBatchDtoMediumResponse();
        PageDto<UploadBatchDtoMediumResponse> pageDto = new PageDto<>(List.of(dto), 0, 15, 1);

        when(uploadBatchService.getFilteredBatches(any(UploadBatchFilter.class), eq(pageable)))
                .thenReturn(pageDto);

        ResponseEntity<Page<UploadBatchDtoMediumResponse>> response = uploadBatchController.getFiltered(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null,
                pageable
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pageDto.toPage(pageable), response.getBody());

        verify(uploadBatchService).getFilteredBatches(any(UploadBatchFilter.class), eq(pageable));
    }
}