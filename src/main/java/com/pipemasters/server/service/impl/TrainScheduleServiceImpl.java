package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.create.TrainScheduleCreateDto;
import com.pipemasters.server.dto.request.update.TrainScheduleUpdateDto;
import com.pipemasters.server.dto.response.TrainScheduleResponseDto;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.exceptions.trainSchedule.TrainScheduleNotFoundException;
import com.pipemasters.server.repository.TrainScheduleRepository;
import com.pipemasters.server.service.ExcelExportService;
import com.pipemasters.server.service.TrainScheduleService;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.pipemasters.server.exceptions.trainSchedule.FileReadException;
import com.pipemasters.server.exceptions.trainSchedule.TrainParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {

    private final Logger log = LoggerFactory.getLogger(TrainScheduleServiceImpl.class);
    private final TrainScheduleRepository trainScheduleRepository;
    private final ModelMapper modelMapper;
    private final ExcelExportService excelExportService;

    public TrainScheduleServiceImpl(TrainScheduleRepository trainScheduleRepository, ModelMapper modelMapper, ExcelExportService excelExportService) {
        this.trainScheduleRepository = trainScheduleRepository;
        this.modelMapper = modelMapper;
        this.excelExportService = excelExportService;
    }

    @Override
    @CacheEvict(value = "trainSchedules", allEntries = true)
    @Transactional
    public ParsingStatsDto parseExcelFile(MultipartFile file) throws IOException {
        log.info("Starting Excel file parsing for file: {}", file.getOriginalFilename());

        List<String> errorMessages = new ArrayList<>();
        int totalRecords = 0, successfullyParsedNew = 0, recordsWithError = 0, existingRecordsInDbFound = 0, updatedRecords = 0;
        Map<String, String> pairedTrainNumbersMap = new HashMap<>();
        Set<String> allRelatedTrainNumbers = new HashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            int rowStart = sheet.getFirstRowNum() + 1;

            Set<String> trainNumbersFromFile = new HashSet<>();
            for (int rowNum = rowStart; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null && row.getCell(0) != null) {
                    String trainNumber = getCellValueAsString(row.getCell(0), formatter);
                    if (!trainNumber.isEmpty()) {
                        trainNumbersFromFile.add(trainNumber);
                    }
                }
            }

            Map<String, TrainSchedule> existingTrainsMap = new HashMap<>();
            if (!trainNumbersFromFile.isEmpty()) {
                existingTrainsMap = trainScheduleRepository.findByTrainNumberIn(trainNumbersFromFile)
                        .stream()
                        .collect(Collectors.toMap(TrainSchedule::getTrainNumber, Function.identity()));
            }

            List<TrainSchedule> trainsToSaveOrUpdate = new ArrayList<>();
            for (int rowNum = rowStart; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || row.getCell(0) == null || getCellValueAsString(row.getCell(0), formatter).isEmpty()) {
                    continue;
                }

                totalRecords++;
                String currentTrainNumberStr = getCellValueAsString(row.getCell(0), formatter);

                try {
                    allRelatedTrainNumbers.add(currentTrainNumberStr);
                    String pairedTrainNumberStr = getCellValueAsString(row.getCell(12), formatter);
                    if (!pairedTrainNumberStr.isEmpty()) {
                        pairedTrainNumbersMap.put(currentTrainNumberStr, pairedTrainNumberStr);
                        allRelatedTrainNumbers.add(pairedTrainNumberStr);
                    }

                    TrainSchedule trainScheduleFromExcel = new TrainSchedule();
                    trainScheduleFromExcel.setTrainNumber(currentTrainNumberStr);
                    fillTrainScheduleFieldsFromRow(trainScheduleFromExcel, row, formatter, rowNum);

                    TrainSchedule existingTrainInDb = existingTrainsMap.get(currentTrainNumberStr);

                    if (existingTrainInDb != null) {
                        existingRecordsInDbFound++;
                        if (areFieldsDifferent(existingTrainInDb, trainScheduleFromExcel)) {
                            updateTrainScheduleFields(existingTrainInDb, trainScheduleFromExcel);
                            trainsToSaveOrUpdate.add(existingTrainInDb);
                            updatedRecords++;
                        }
                    } else {
                        trainsToSaveOrUpdate.add(trainScheduleFromExcel);
                        successfullyParsedNew++;
                    }
                } catch (Exception e) {
                    recordsWithError++;
                    errorMessages.add("Error processing row " + (rowNum + 1) + " for train " + currentTrainNumberStr + ": " + e.getMessage());
                    log.warn("Error on row {}: {}", rowNum + 1, e.getMessage());
                }
            }

            if (!trainsToSaveOrUpdate.isEmpty()) {
                log.info("Saving/updating {} train schedules to the database.", trainsToSaveOrUpdate.size());
                trainScheduleRepository.saveAll(trainsToSaveOrUpdate);
                log.info("Finished saving/updating train schedules.");
            }

            if (!pairedTrainNumbersMap.isEmpty()) {
                log.info("Starting to process paired train links for {} entries.", pairedTrainNumbersMap.size());
                Map<String, TrainSchedule> allTrainsForLinking = trainScheduleRepository.findByTrainNumberIn(allRelatedTrainNumbers)
                        .stream()
                        .collect(Collectors.toMap(TrainSchedule::getTrainNumber, Function.identity()));

                List<TrainSchedule> trainsWithUpdatedPairedLinks = new ArrayList<>();
                for (Map.Entry<String, String> entry : pairedTrainNumbersMap.entrySet()) {
                    String currentTrainNumber = entry.getKey();
                    String pairedTrainNumber = entry.getValue();

                    TrainSchedule currentTrain = allTrainsForLinking.get(currentTrainNumber);
                    TrainSchedule pairedTrain = allTrainsForLinking.get(pairedTrainNumber);

                    if (currentTrain != null) {
                        String currentPairedTrainNumberInDb = (currentTrain.getPairTrain() != null) ? currentTrain.getPairTrain().getTrainNumber() : null;
                        if (!Objects.equals(currentPairedTrainNumberInDb, pairedTrainNumber)) {
                            if (pairedTrain != null) {
                                currentTrain.setPairTrain(pairedTrain);
                                trainsWithUpdatedPairedLinks.add(currentTrain);
                            } else {
                                recordsWithError++;
                                errorMessages.add("Paired train " + pairedTrainNumber + " not found for train " + currentTrainNumber);
                            }
                        }
                    } else {
                        recordsWithError++;
                        errorMessages.add("Train " + currentTrainNumber + " not found for linking.");
                    }
                }

                if (!trainsWithUpdatedPairedLinks.isEmpty()) {
                    log.info("Saving {} train schedules with updated paired links.", trainsWithUpdatedPairedLinks.size());
                    trainScheduleRepository.saveAll(trainsWithUpdatedPairedLinks);
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse Excel file due to an unexpected error.", e);
            throw new IOException("Failed to parse Excel file.", e);
        }

        ParsingStatsDto stats = new ParsingStatsDto(
                totalRecords,
                successfullyParsedNew,
                recordsWithError,
                existingRecordsInDbFound,
                updatedRecords,
                errorMessages
        );
        log.info("Excel parsing finished. Stats: {}", stats);
        return stats;
    }

    private void fillTrainScheduleFieldsFromRow(TrainSchedule trainSchedule, Row row, DataFormatter formatter, int rowNum) {
        try {
            trainSchedule.setCategory(getCellValueAsString(row.getCell(1), formatter));
            trainSchedule.setDepartureStation(getCellValueAsString(row.getCell(2), formatter));
            trainSchedule.setArrivalStation(getCellValueAsString(row.getCell(3), formatter));
            trainSchedule.setCustomName(getCellValueAsString(row.getCell(4), formatter));
            trainSchedule.setRailwayInfo(getCellValueAsString(row.getCell(5), formatter));
            trainSchedule.setTravelTime(parseDuration(getCellValueAsString(row.getCell(6), formatter)));
            trainSchedule.setDepartureTime(parseTime(getCellValueAsString(row.getCell(7), formatter)));
            trainSchedule.setArrivalTime(parseTime(getCellValueAsString(row.getCell(8), formatter)));
            trainSchedule.setFirm(parseFirmness(getCellValueAsString(row.getCell(9), formatter)));
            trainSchedule.setPeriodicity(getCellValueAsString(row.getCell(10), formatter));
            trainSchedule.setSeasonality(getCellValueAsString(row.getCell(11), formatter));
        } catch (Exception e) {
            log.error("Error filling fields for train {} from row {}: {}", trainSchedule.getTrainNumber(), rowNum + 1, e.getMessage(), e);
            throw new TrainParsingException("Error when filling in train fields: " + e.getMessage(), e);
        }
    }

    private void updateTrainScheduleFields(TrainSchedule existingTrain, TrainSchedule newTrainData) {
        existingTrain.setCategory(newTrainData.getCategory());
        existingTrain.setDepartureStation(newTrainData.getDepartureStation());
        existingTrain.setArrivalStation(newTrainData.getArrivalStation());
        existingTrain.setCustomName(newTrainData.getCustomName());
        existingTrain.setRailwayInfo(newTrainData.getRailwayInfo());
        existingTrain.setTravelTime(newTrainData.getTravelTime());
        existingTrain.setDepartureTime(newTrainData.getDepartureTime());
        existingTrain.setArrivalTime(newTrainData.getArrivalTime());
        existingTrain.setFirm(newTrainData.isFirm());
        existingTrain.setPeriodicity(newTrainData.getPeriodicity());
        existingTrain.setSeasonality(newTrainData.getSeasonality());
    }

    private boolean areFieldsDifferent(TrainSchedule existingTrain, TrainSchedule newTrainData) {
        return !Objects.equals(existingTrain.getCategory(), newTrainData.getCategory()) ||
                !Objects.equals(existingTrain.getDepartureStation(), newTrainData.getDepartureStation()) ||
                !Objects.equals(existingTrain.getArrivalStation(), newTrainData.getArrivalStation()) ||
                !Objects.equals(existingTrain.getCustomName(), newTrainData.getCustomName()) ||
                !Objects.equals(existingTrain.getRailwayInfo(), newTrainData.getRailwayInfo()) ||
                !Objects.equals(existingTrain.getTravelTime(), newTrainData.getTravelTime()) ||
                !Objects.equals(existingTrain.getDepartureTime(), newTrainData.getDepartureTime()) ||
                !Objects.equals(existingTrain.getArrivalTime(), newTrainData.getArrivalTime()) ||
                existingTrain.isFirm() != newTrainData.isFirm() ||
                !Objects.equals(existingTrain.getPeriodicity(), newTrainData.getPeriodicity()) ||
                !Objects.equals(existingTrain.getSeasonality(), newTrainData.getSeasonality());
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private Duration parseDuration(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            String[] parts = timeStr.split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return Duration.ofHours(hours).plusMinutes(minutes);
            } else {
                throw new TrainParsingException("Incorrect travel time format '" + timeStr + "'. Expected HH:MM.");
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse duration '{}': {}", timeStr, e.getMessage());
            throw new TrainParsingException("Incorrect travel time format '" + timeStr + "'. Expected HH:MM.", e);
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            throw new TrainParsingException("Incorrect time format '" + timeStr + "'. Expected HH:MM.", e);
        }
    }

    private boolean parseFirmness(String firmnessStr) {
        return "Фирменный".equalsIgnoreCase(firmnessStr.trim());
    }

    @Override
    @CacheEvict(value = "trainSchedules", allEntries = true)
    @Transactional
    public TrainScheduleResponseDto create(TrainScheduleCreateDto requestDto) {
        log.info("Creating new train schedule for train number: {}", requestDto.getTrainNumber());
        TrainSchedule trainSchedule = modelMapper.map(requestDto, TrainSchedule.class);

        if (requestDto.getPairTrainId() != null) {
            TrainSchedule pairTrain = trainScheduleRepository.findById(requestDto.getPairTrainId())
                    .orElseThrow(() -> new TrainScheduleNotFoundException("Paired train with id " + requestDto.getPairTrainId() + " not found."));
            trainSchedule.setPairTrain(pairTrain);
        }

        TrainSchedule savedTrainSchedule = trainScheduleRepository.save(trainSchedule);
        log.info("Successfully created train schedule with id: {}", savedTrainSchedule.getId());
        return modelMapper.map(savedTrainSchedule, TrainScheduleResponseDto.class);
    }

    @Override
    @Cacheable("trainSchedules")
    @Transactional(readOnly = true)
    public PageDto<TrainScheduleResponseDto> getAllPaginated(Pageable pageable) {
        log.info("Fetching paginated train schedules with pageable: {}", pageable);
        Page<TrainSchedule> page = trainScheduleRepository.findAll(pageable);
        List<TrainScheduleResponseDto> dtoList = page.stream()
                .map(schedule -> modelMapper.map(schedule, TrainScheduleResponseDto.class)).toList();

        return new PageDto<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainScheduleResponseDto getById(Long id) {
        log.info("Fetching train schedule with id: {}", id);
        TrainSchedule trainSchedule = trainScheduleRepository.findById(id)
                .orElseThrow(() -> new TrainScheduleNotFoundException("Train schedule with id " + id + " not found."));
        return modelMapper.map(trainSchedule, TrainScheduleResponseDto.class);
    }

    @Override
    @CacheEvict(value = "trainSchedules", allEntries = true)
    @Transactional
    public TrainScheduleResponseDto update(Long id, TrainScheduleUpdateDto updateDto) {
        log.info("Updating train schedule with id: {}", id);
        TrainSchedule existingTrainSchedule = trainScheduleRepository.findById(id)
                .orElseThrow(() -> new TrainScheduleNotFoundException("Train schedule with id " + id + " not found."));

        modelMapper.map(updateDto, existingTrainSchedule);
        existingTrainSchedule.setId(id);

        if (updateDto.getPairTrainId() != null) {
            TrainSchedule pairTrain = trainScheduleRepository.findById(updateDto.getPairTrainId())
                    .orElseThrow(() -> new TrainScheduleNotFoundException("Paired train with id " + updateDto.getPairTrainId() + " not found."));
            existingTrainSchedule.setPairTrain(pairTrain);
        }

        TrainSchedule updatedTrainSchedule = trainScheduleRepository.save(existingTrainSchedule);
        log.info("Successfully updated train schedule with id: {}", updatedTrainSchedule.getId());
        return modelMapper.map(updatedTrainSchedule, TrainScheduleResponseDto.class);
    }

    @Override
    @CacheEvict(value = "trainSchedules", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Set<Long> idsToDelete = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();

        if (trainScheduleRepository.existsById(id)) {
            queue.add(id);
            idsToDelete.add(id);
        }

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();

            trainScheduleRepository.findById(currentId).ifPresent(train -> {
                if (train.getPairTrain() != null) {
                    Long pairId = train.getPairTrain().getId();
                    if (idsToDelete.add(pairId)) {
                        queue.add(pairId);
                    }
                }
            });
        }

        if (idsToDelete.isEmpty()) {
            log.warn("Train schedule with id {} not found for deletion.", id);
            throw new TrainScheduleNotFoundException("Train schedule with id " + id + " not found for deletion.");
        }

        List<TrainSchedule> referencingSchedules = trainScheduleRepository.findByPairTrainIdIn(idsToDelete);
        if (!referencingSchedules.isEmpty()) {
            for (TrainSchedule schedule : referencingSchedules) {
                schedule.setPairTrain(null);
            }
            trainScheduleRepository.saveAll(referencingSchedules);
        }
        trainScheduleRepository.deleteAllByIdInBatch(idsToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportSchedulesToExcel() throws IOException {
        log.info("Starting export of all train schedules to Excel.");
        List<TrainSchedule> schedules = trainScheduleRepository.findAllByOrderByTrainNumberAsc();
        ByteArrayOutputStream outputStream = excelExportService.exportTrainScheduleToExcel(schedules);
        return outputStream;
    }
}