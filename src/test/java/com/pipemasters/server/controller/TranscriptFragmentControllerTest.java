package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptFragmentControllerTest {

    @Mock
    private TranscriptFragmentService transcriptService;

    @InjectMocks
    private TranscriptFragmentController controller;

    @Test
    void search_UploadBatch() {
        String query = "q";
        Pageable pageable = PageRequest.of(0, 10);
        UploadBatchDtoSmallResponse responseDto = new UploadBatchDtoSmallResponse();
        PageDto<UploadBatchDtoSmallResponse> expectedPage = new PageDto<>(List.of(responseDto), 0, 10, 1);
        when(transcriptService.searchUploadBatches(query, pageable)).thenReturn(expectedPage);
        ResponseEntity<PageDto<UploadBatchDtoSmallResponse>> response = controller.search(query, pageable);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(transcriptService).searchUploadBatches(query, pageable);
    }

    @Test
    void searchByUploadBatch_ReturnsData() {
        MediaFileFragmentsDto dto = new MediaFileFragmentsDto(1L, List.of(2L));
        when(transcriptService.searchByUploadBatch(5L, "q")).thenReturn(List.of(dto));

        ResponseEntity<List<MediaFileFragmentsDto>> response = controller.searchByUploadBatch(5L, "q");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(dto), response.getBody());
        verify(transcriptService).searchByUploadBatch(5L, "q");
    }

    @Test
    void fetchFromExternal_ReturnsOk() {
        ResponseEntity<Void> response = controller.fetchFromExternal(1L, "call");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transcriptService).fetchFromExternal(1L, "call");
    }

    @Test
    void get_ReturnsFragments() {
        SttFragmentDto dto = new SttFragmentDto();
        when(transcriptService.getByMediaFile(3L)).thenReturn(List.of(dto));

        ResponseEntity<List<SttFragmentDto>> response = controller.get(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(dto), response.getBody());
        verify(transcriptService).getByMediaFile(3L);
    }
}
