package com.pipemasters.server.service;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.exceptions.trainSchedule.FileReadException;
import com.pipemasters.server.repository.TrainScheduleRepository;
import com.pipemasters.server.service.impl.TrainScheduleServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainScheduleServiceImplTest {

    @Mock
    private TrainScheduleRepository trainScheduleRepository;

    @InjectMocks
    private TrainScheduleServiceImpl trainScheduleService;

    private MockMultipartFile createExcelFile(String[][] data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Расписание");

            sheet.createRow(0);
            sheet.createRow(1);

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 2);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }
            workbook.write(outputStream);
            return new MockMultipartFile("file", "schedule.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outputStream.toByteArray());
        }
    }

    private MockMultipartFile createBrokenExcelFile() {
        return new MockMultipartFile("file", "broken.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "not-a-valid-excel-content".getBytes());
    }

    private String[][] getValidTrainData() {
        return new String[][]{
                {"0001А", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", "0002А"},
                {"0002А", "Скорые", "МСК", "СПБ", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", "0001А"}
        };
    }

    private String[][] getInvalidTrainDataEmptyNumber() {
        return new String[][]{
                {"", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""},
        };
    }

    private String[][] getInvalidTrainDataBadTimeFormat() {
        return new String[][]{
                {"0003В", "Скорые", "СПБ", "НВГ", "Тестовый", "РЖД", "09-00", "12:00", "08:00", "Обычный", "Ежедневно", "Круглогодичный", ""},
        };
    }

    @Test
    @DisplayName("Should successfully parse and save new train schedules")
    void parseExcelFile_shouldParseAndSaveNewTrains() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001А", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""},
                {"0002Б", "Обычные", "КЗН", "ЕКБ", "Уральский", "РЖД", "15:00", "10:00", "01:00", "Обычный", "Пн,Ср,Пт", "Лето", ""}
        });

        when(trainScheduleRepository.findByTrainNumber(anyString())).thenReturn(Optional.empty());

        when(trainScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<TrainSchedule> savedTrains = invocation.getArgument(0);
            savedTrains.forEach(ts -> {
                if (ts.getId() == null) ts.setId(1L);
            });
            return savedTrains;
        });


        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertEquals(0, result.getExistingRecordsInDb());
        assertEquals(0, result.getUpdatedRecords());
        assertTrue(result.getErrorMessages().isEmpty());

        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(1)).saveAll(captor.capture());
        List<TrainSchedule> savedTrains = captor.getValue();
        assertEquals(2, savedTrains.size());

        TrainSchedule train1 = savedTrains.stream().filter(t -> "0001А".equals(t.getTrainNumber())).findFirst().orElseThrow();
        assertEquals("Скорые", train1.getCategory());
        assertEquals(LocalTime.of(23, 30), train1.getDepartureTime());
        assertEquals(Duration.ofHours(9), train1.getTravelTime());
        assertTrue(train1.isFirm());
        assertNull(train1.getPairTrain());

        TrainSchedule train2 = savedTrains.stream().filter(t -> "0002Б".equals(t.getTrainNumber())).findFirst().orElseThrow();
        assertEquals("Обычные", train2.getCategory());
        assertFalse(train2.isFirm());
    }

    @Test
    @DisplayName("Should update existing train schedules")
    void parseExcelFile_shouldUpdateExistingTrains() throws IOException {
        String trainNumberToUpdate = "0001А";
        MockMultipartFile file = createExcelFile(new String[][]{
                {trainNumberToUpdate, "Скорые-Обновленные", "СПБ", "МСК", "Красная стрела-НОВАЯ", "РЖД", "09:15", "23:45", "08:45", "Фирменный", "Ежедневно", "Круглогодичный", ""}
        });

        TrainSchedule existingTrain = new TrainSchedule();
        existingTrain.setTrainNumber(trainNumberToUpdate);
        existingTrain.setCategory("Старые скорые");
        existingTrain.setDepartureTime(LocalTime.of(23, 0));
        existingTrain.setFirm(false);

        when(trainScheduleRepository.findByTrainNumber(trainNumberToUpdate)).thenReturn(Optional.of(existingTrain));
        when(trainScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertEquals(1, result.getExistingRecordsInDb());
        assertEquals(1, result.getUpdatedRecords());
        assertTrue(result.getErrorMessages().isEmpty());


        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(1)).saveAll(captor.capture());
        List<TrainSchedule> savedTrains = captor.getValue();
        assertEquals(1, savedTrains.size());
        assertEquals(existingTrain, savedTrains.get(0));

        assertEquals("Скорые-Обновленные", existingTrain.getCategory());
        assertEquals(LocalTime.of(23, 45), existingTrain.getDepartureTime());
        assertTrue(existingTrain.isFirm());
    }

    @Test
    @DisplayName("Should handle parsing errors for rows and collect messages")
    void parseExcelFile_shouldHandleRowParsingErrors() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001Д", "Скорые", "СПБ", "МСК", "Т", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""},
                {"", "Скорые", "КЗН", "ЕКБ", "У", "РЖД", "15:00", "10:00", "01:00", "Обычный", "Пн,Ср,Пт", "Лето", ""},
                {"0003Ж", "Скорые", "ВЛГ", "ПРМ", "Р", "РЖД", "invalid-time", "12:00", "07:00", "Фирменный", "Ежедневно", "Круглогодичный", ""},
        });

        when(trainScheduleRepository.findByTrainNumber(anyString())).thenReturn(Optional.empty());
        when(trainScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getSuccessfullyParsed());
        assertEquals(1, result.getRecordsWithError());
        assertEquals(0, result.getExistingRecordsInDb());
        assertEquals(0, result.getUpdatedRecords());
        assertFalse(result.getErrorMessages().isEmpty());
        assertEquals(1, result.getErrorMessages().size());

        assertTrue(result.getErrorMessages().get(0).contains("Error parsing row 5: Error when filling in train fields: Incorrect travel time format 'invalid-time'. Expected HH:MM."));


        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(1)).saveAll(captor.capture());
        List<TrainSchedule> savedTrains = captor.getValue();
        assertEquals(1, savedTrains.size());
        assertEquals("0001Д", savedTrains.get(0).getTrainNumber());
    }

    @Test
    @DisplayName("Should throw FileReadException for a malformed Excel file")
    void parseExcelFile_shouldThrowFileReadExceptionForMalformedFile() throws IOException {
        MockMultipartFile brokenFile = createBrokenExcelFile();

        FileReadException thrown = assertThrows(FileReadException.class, () -> trainScheduleService.parseExcelFile(brokenFile));

        assertTrue(thrown.getMessage().contains("It is not possible to create a Workbook from a file."));
        verify(trainScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should correctly link paired trains")
    void parseExcelFile_shouldLinkPairedTrains() throws IOException {
        MockMultipartFile file = createExcelFile(getValidTrainData());

        TrainSchedule train1A = new TrainSchedule();
        train1A.setTrainNumber("0001А");
        train1A.setCategory("Скорые");
        train1A.setDepartureTime(LocalTime.of(23, 30));
        train1A.setTravelTime(Duration.ofHours(9));
        train1A.setFirm(true);

        TrainSchedule train2A = new TrainSchedule();
        train2A.setTrainNumber("0002А");
        train2A.setCategory("Скорые");
        train2A.setDepartureTime(LocalTime.of(23, 30));
        train2A.setTravelTime(Duration.ofHours(9));
        train2A.setFirm(true);

        when(trainScheduleRepository.findByTrainNumber("0001А")).thenReturn(Optional.empty());
        when(trainScheduleRepository.findByTrainNumber("0002А")).thenReturn(Optional.empty());

        when(trainScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<TrainSchedule> newTrains = invocation.getArgument(0);
            newTrains.forEach(t -> {
                if ("0001А".equals(t.getTrainNumber())) {
                    t.setId(1L);
                } else if ("0002А".equals(t.getTrainNumber())) {
                    t.setId(2L);
                }
            });
            return newTrains;
        });

        when(trainScheduleRepository.findByTrainNumberIn(anySet())).thenReturn(Arrays.asList(train1A, train2A));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertTrue(result.getErrorMessages().isEmpty());

        verify(trainScheduleRepository, times(2)).saveAll(anyList());

        assertEquals(train2A, train1A.getPairTrain());
        assertEquals(train1A, train2A.getPairTrain());

        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(2)).saveAll(captor.capture());
        List<TrainSchedule> updatedTrains = captor.getAllValues().get(1);
        assertEquals(2, updatedTrains.size());
        assertTrue(updatedTrains.contains(train1A));
        assertTrue(updatedTrains.contains(train2A));
    }


    @Test
    @DisplayName("Should handle missing paired train during linking phase")
    void parseExcelFile_shouldHandleMissingPairedTrain() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001А", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", "0002Б"},
                {"0003В", "Скорые", "КЗН", "ЕКБ", "Тест", "РЖД", "10:00", "12:00", "15:00", "Обычный", "Ежедневно", "Круглогодичный", ""},
        });

        TrainSchedule train1A = new TrainSchedule();
        train1A.setTrainNumber("0001А");
        train1A.setId(1L);

        TrainSchedule train3V = new TrainSchedule();
        train3V.setTrainNumber("0003В");
        train3V.setId(3L);

        when(trainScheduleRepository.findByTrainNumber("0001А")).thenReturn(Optional.empty());
        when(trainScheduleRepository.findByTrainNumber("0003В")).thenReturn(Optional.empty());

        when(trainScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<TrainSchedule> newTrains = invocation.getArgument(0);
            newTrains.forEach(t -> {
                if ("0001А".equals(t.getTrainNumber())) t.setId(1L);
                else if ("0003В".equals(t.getTrainNumber())) t.setId(3L);
            });
            return newTrains;
        });

        when(trainScheduleRepository.findByTrainNumberIn(anySet())).thenReturn(Arrays.asList(train1A, train3V));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfullyParsed());
        assertEquals(1, result.getRecordsWithError());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Couldn't establish connection for train 0001А: paired train 0002Б not found in the database."));

        verify(trainScheduleRepository, times(1)).saveAll(anyList());

        assertNull(train1A.getPairTrain());
    }

    @Test
    @DisplayName("Should not update existing train if no fields are different")
    void parseExcelFile_shouldNotUpdateIfNoDifferences() throws IOException {
        String trainNumber = "0001А";
        MockMultipartFile file = createExcelFile(new String[][]{
                {trainNumber, "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""}
        });

        TrainSchedule existingTrain = new TrainSchedule();
        existingTrain.setTrainNumber(trainNumber);
        existingTrain.setCategory("Скорые");
        existingTrain.setDepartureStation("СПБ");
        existingTrain.setArrivalStation("МСК");
        existingTrain.setCustomName("Красная стрела");
        existingTrain.setRailwayInfo("РЖД");
        existingTrain.setTravelTime(Duration.ofHours(9));
        existingTrain.setDepartureTime(LocalTime.of(23, 30));
        existingTrain.setArrivalTime(LocalTime.of(8, 30));
        existingTrain.setFirm(true);
        existingTrain.setPeriodicity("Ежедневно");
        existingTrain.setSeasonality("Круглогодичный");

        when(trainScheduleRepository.findByTrainNumber(trainNumber)).thenReturn(Optional.of(existingTrain));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertEquals(1, result.getExistingRecordsInDb());
        assertEquals(0, result.getUpdatedRecords());
        assertTrue(result.getErrorMessages().isEmpty());

        verify(trainScheduleRepository, never()).saveAll(argThat(list -> list.equals(existingTrain)));
    }
}
