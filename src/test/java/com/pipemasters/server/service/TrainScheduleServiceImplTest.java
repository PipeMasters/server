package com.pipemasters.server.service;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.repository.TrainScheduleRepository;
import com.pipemasters.server.service.impl.TrainScheduleServiceImpl;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainScheduleServiceImplTest {

    @Mock
    private TrainScheduleRepository trainScheduleRepository;

    @Mock
    private ExcelExportService excelExportService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private TrainScheduleServiceImpl trainScheduleService;

    private MockMultipartFile createExcelFile(String[][] data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Расписание");
            sheet.createRow(0);

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
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

    @Test
    @DisplayName("Should successfully parse and save new train schedules")
    void parseExcelFile_shouldParseAndSaveNewTrains() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001А", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""},
                {"0002Б", "Обычные", "КЗН", "ЕКБ", "Уральский", "РЖД", "15:00", "10:00", "01:00", "Обычный", "Пн,Ср,Пт", "Лето", ""}
        });

        when(trainScheduleRepository.findByTrainNumberIn(anySet())).thenReturn(Collections.emptyList());

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
                {trainNumberToUpdate, "Скорые-Обновленные", "СПБ", "МСК", "Красная стрела-НОВАЯ", "РЖД", "09:15", "23:45", "09:00", "Фирменный", "Ежедневно", "Круглогодичный", ""}
        });

        TrainSchedule existingTrain = new TrainSchedule();
        existingTrain.setId(1L);
        existingTrain.setTrainNumber(trainNumberToUpdate);
        existingTrain.setCategory("Старые скорые");
        existingTrain.setDepartureTime(LocalTime.of(23, 0));
        existingTrain.setFirm(false);

        when(trainScheduleRepository.findByTrainNumberIn(Set.of(trainNumberToUpdate))).thenReturn(List.of(existingTrain));

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
        TrainSchedule updatedTrain = savedTrains.get(0);

        assertEquals("Скорые-Обновленные", updatedTrain.getCategory());
        assertEquals(LocalTime.of(23, 45), updatedTrain.getDepartureTime());
        assertTrue(updatedTrain.isFirm());
    }

    @Test
    @DisplayName("Should handle parsing errors for rows and collect messages")
    void parseExcelFile_shouldHandleRowParsingErrors() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001Д", "Скорые", "СПБ", "МСК", "Т", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", ""},
                {"", "Скорые", "КЗН", "ЕКБ", "У", "РЖД", "15:00", "10:00", "01:00", "Обычный", "Пн,Ср,Пт", "Лето", ""},
                {"0003Ж", "Скорые", "ВЛГ", "ПРМ", "Р", "РЖД", "invalid-time", "12:00", "07:00", "Фирменный", "Ежедневно", "Круглогодичный", ""},
        });

        when(trainScheduleRepository.findByTrainNumberIn(anySet())).thenReturn(Collections.emptyList());

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getSuccessfullyParsed());
        assertEquals(1, result.getRecordsWithError());
        assertEquals(0, result.getExistingRecordsInDb());
        assertEquals(0, result.getUpdatedRecords());
        assertFalse(result.getErrorMessages().isEmpty());
        assertEquals(1, result.getErrorMessages().size());

        String expectedErrorMessage = "Error processing row 4 for train 0003Ж: Error when filling in train fields: Incorrect travel time format 'invalid-time'. Expected HH:MM.";
        assertTrue(result.getErrorMessages().get(0).contains(expectedErrorMessage));

        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(1)).saveAll(captor.capture());
        List<TrainSchedule> savedTrains = captor.getValue();
        assertEquals(1, savedTrains.size());
        assertEquals("0001Д", savedTrains.get(0).getTrainNumber());
    }

    @Test
    @DisplayName("Should throw IOException for a malformed Excel file")
    void parseExcelFile_shouldThrowIOExceptionForMalformedFile() {
        MockMultipartFile brokenFile = createBrokenExcelFile();
        IOException thrown = assertThrows(IOException.class, () -> trainScheduleService.parseExcelFile(brokenFile));
        assertTrue(thrown.getMessage().contains("Failed to parse Excel file."));
        verify(trainScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should correctly link paired trains")
    void parseExcelFile_shouldLinkPairedTrains() throws IOException {
        MockMultipartFile file = createExcelFile(getValidTrainData());

        TrainSchedule train1A = new TrainSchedule();
        train1A.setId(1L);
        train1A.setTrainNumber("0001А");

        TrainSchedule train2A = new TrainSchedule();
        train2A.setId(2L);
        train2A.setTrainNumber("0002А");

        when(trainScheduleRepository.findByTrainNumberIn(anySet()))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(train1A, train2A));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertTrue(result.getErrorMessages().isEmpty());

        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(2)).saveAll(captor.capture());

        List<TrainSchedule> linkedTrains = captor.getAllValues().get(1);
        TrainSchedule linkedTrain1 = linkedTrains.stream().filter(t -> t.getTrainNumber().equals("0001А")).findFirst().orElseThrow();
        TrainSchedule linkedTrain2 = linkedTrains.stream().filter(t -> t.getTrainNumber().equals("0002А")).findFirst().orElseThrow();

        assertNotNull(linkedTrain1.getPairTrain());
        assertNotNull(linkedTrain2.getPairTrain());
        assertEquals("0002А", linkedTrain1.getPairTrain().getTrainNumber());
        assertEquals("0001А", linkedTrain2.getPairTrain().getTrainNumber());
    }

    @Test
    @DisplayName("Should handle missing paired train during linking phase")
    void parseExcelFile_shouldHandleMissingPairedTrain() throws IOException {
        MockMultipartFile file = createExcelFile(new String[][]{
                {"0001А", "Скорые", "СПБ", "МСК", "Красная стрела", "РЖД", "09:00", "23:30", "08:30", "Фирменный", "Ежедневно", "Круглогодичный", "0002Б"},
                {"0003В", "Скорые", "КЗН", "ЕКБ", "Тест", "РЖД", "10:00", "12:00", "22:00", "Обычный", "Ежедневно", "Круглогодичный", ""},
        });

        TrainSchedule train1A = new TrainSchedule();
        train1A.setId(1L);
        train1A.setTrainNumber("0001А");

        TrainSchedule train3V = new TrainSchedule();
        train3V.setId(3L);
        train3V.setTrainNumber("0003В");

        when(trainScheduleRepository.findByTrainNumberIn(anySet()))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(train1A, train3V));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfullyParsed());
        assertEquals(1, result.getRecordsWithError());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Paired train 0002Б not found for train 0001А"));

        verify(trainScheduleRepository, times(1)).saveAll(any());

        ArgumentCaptor<List<TrainSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainScheduleRepository, times(1)).saveAll(captor.capture());

        TrainSchedule savedTrain1A = captor.getValue().stream().filter(t -> t.getTrainNumber().equals("0001А")).findFirst().orElseThrow();
        assertNull(savedTrain1A.getPairTrain());
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

        when(trainScheduleRepository.findByTrainNumberIn(Set.of(trainNumber))).thenReturn(List.of(existingTrain));

        ParsingStatsDto result = trainScheduleService.parseExcelFile(file);

        assertNotNull(result);
        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfullyParsed());
        assertEquals(0, result.getRecordsWithError());
        assertEquals(1, result.getExistingRecordsInDb());
        assertEquals(0, result.getUpdatedRecords());
        assertTrue(result.getErrorMessages().isEmpty());

        verify(trainScheduleRepository, never()).saveAll(any());
    }
}