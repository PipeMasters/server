package com.pipemasters.server.controller;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.exceptions.trainSchedule.FileReadException;
import com.pipemasters.server.service.TrainScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainScheduleControllerTest {

    @Mock
    private TrainScheduleService trainScheduleService;

    @InjectMocks
    private TrainScheduleController trainScheduleController;

    @Test
    @DisplayName("Should return 200 OK and parsing stats on successful file upload")
    void uploadExcelFile_success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "schedule.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test data".getBytes()
        );

        ParsingStatsDto expectedStats = new ParsingStatsDto(
                10,
                8,
                2,
                0,
                0,
                Collections.singletonList("Error in row 5: Some detail")
        );

        when(trainScheduleService.parseExcelFile(any(MultipartFile.class)))
                .thenReturn(expectedStats);

        ResponseEntity<ParsingStatsDto> response = trainScheduleController.uploadExcelFile(mockFile);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedStats, response.getBody());

        assertEquals(10, response.getBody().getTotalRecords());
        assertEquals(8, response.getBody().getSuccessfullyParsed());
        assertEquals(2, response.getBody().getRecordsWithError());
        assertEquals("Error in row 5: Some detail", response.getBody().getErrorMessages().get(0));
    }


    @Test
    @DisplayName("Should return 400 BAD REQUEST if the uploaded file is empty")
    void uploadExcelFile_emptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        ResponseEntity<ParsingStatsDto> response = trainScheduleController.uploadExcelFile(emptyFile);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getErrorMessages().size());
        assertEquals("The file to download is missing or empty.", response.getBody().getErrorMessages().get(0));
        assertEquals(0, response.getBody().getTotalRecords());
        assertEquals(0, response.getBody().getSuccessfullyParsed());
    }

    @Test
    @DisplayName("Should handle IOException from service and rethrow it (or return 500 in full context)")
    void uploadExcelFile_serviceThrowsIOException() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "corrupted.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "corrupted data".getBytes()
        );

        IOException expectedException = new IOException("Simulated file read error");
        when(trainScheduleService.parseExcelFile(any(MultipartFile.class)))
                .thenThrow(expectedException);

        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> {
            trainScheduleController.uploadExcelFile(mockFile);
        });

        assertNotNull(thrown);
        assertEquals(expectedException.getMessage(), thrown.getMessage());
    }


    @Test
    @DisplayName("Should handle FileReadException from service and return 500 INTERNAL SERVER ERROR")
    void uploadExcelFile_serviceThrowsFileReadException() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "corrupted.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "corrupted data".getBytes()
        );

        FileReadException expectedException = new FileReadException("It is not possible to create a Workbook from a file.");
        when(trainScheduleService.parseExcelFile(any(MultipartFile.class)))
                .thenThrow(expectedException);

        Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(FileReadException.class, () -> {
            trainScheduleController.uploadExcelFile(mockFile);
        });

        assertNotNull(thrown);
        assertEquals(expectedException.getMessage(), thrown.getMessage());
    }
}