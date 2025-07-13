package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.repository.TrainScheduleRepository;
import com.pipemasters.server.service.TrainScheduleService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.pipemasters.server.exceptions.trainSchedule.FileReadException;
import com.pipemasters.server.exceptions.trainSchedule.TrainParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {

    private final Logger log = LoggerFactory.getLogger(TrainScheduleServiceImpl.class);
    private final TrainScheduleRepository trainScheduleRepository;

    public TrainScheduleServiceImpl(TrainScheduleRepository trainScheduleRepository) {
        this.trainScheduleRepository = trainScheduleRepository;
    }

    @Override
    @Transactional
    public ParsingStatsDto parseExcelFile(MultipartFile file) throws IOException {
        log.info("Starting Excel file parsing for file: {}", file.getOriginalFilename());

        List<TrainSchedule> trainsToSaveOrUpdate = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        int totalRecords = 0;
        int successfullyParsedNew = 0;
        int recordsWithError = 0;
        int existingRecordsInDbFound = 0;
        int updatedRecords = 0;

        Map<String, String> pairedTrainNumbersMap = new HashMap<>();
        Set<String> allRelatedTrainNumbers = new HashSet<>();

        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook;
            try {
                workbook = WorkbookFactory.create(is);
            } catch (Exception e) {
                log.error("Failed to create Workbook from file: {}. File might be corrupted or has an unsupported format.", file.getOriginalFilename(), e);
                throw new FileReadException("It is not possible to create a Workbook from a file. The file may be corrupted or has an unsupported format.", e);
            }

            Sheet sheet = workbook.getSheetAt(0);
            log.debug("Processing sheet at index 0. Last row number: {}", sheet.getLastRowNum());

            int rowStart = sheet.getFirstRowNum();
            if (sheet.getPhysicalNumberOfRows() > 2) {
                rowStart += 2;
            }

            for (int rowNum = rowStart; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);

                if (row == null) {
                    log.trace("Skipping null row: {}", rowNum + 1);
                    continue;
                }

                Cell firstCell = row.getCell(0);

                if (firstCell == null || getCellValueAsString(firstCell, formatter).trim().isEmpty()) {
                    log.trace("Skipping empty or blank row (first cell is null or empty after trim): {}", rowNum + 1);
                    continue;
                }

                totalRecords++;

                try {
                    String currentTrainNumberStr = getCellValueAsString(firstCell, formatter);
                    log.trace("Row {}: Train number found: {}", rowNum + 1, currentTrainNumberStr);


                    if (currentTrainNumberStr.isEmpty()) {
                        throw new TrainParsingException("The train number cannot be empty.");
                    }
                    allRelatedTrainNumbers.add(currentTrainNumberStr);

                    String pairedTrainNumberStr = getCellValueAsString(row.getCell(12), formatter);
                    if (!pairedTrainNumberStr.isEmpty()) {
                        pairedTrainNumbersMap.put(currentTrainNumberStr, pairedTrainNumberStr);
                        allRelatedTrainNumbers.add(pairedTrainNumberStr);
                        log.debug("Row {}: Paired train number {} found for train {}.", rowNum + 1, pairedTrainNumberStr, currentTrainNumberStr);
                    }

                    Optional<TrainSchedule> existingTrainOptional = trainScheduleRepository.findByTrainNumber(currentTrainNumberStr);
                    TrainSchedule trainScheduleFromExcel = new TrainSchedule();
                    trainScheduleFromExcel.setTrainNumber(currentTrainNumberStr);
                    fillTrainScheduleFieldsFromRow(trainScheduleFromExcel, row, formatter, rowNum);

                    if (existingTrainOptional.isPresent()) {
                        existingRecordsInDbFound++;
                        TrainSchedule existingTrainInDb = existingTrainOptional.get();
                        log.debug("Row {}: Train {} found in database. Checking for updates.", rowNum + 1, currentTrainNumberStr);


                        if (areFieldsDifferent(existingTrainInDb, trainScheduleFromExcel)) {
                            updateTrainScheduleFields(existingTrainInDb, trainScheduleFromExcel);
                            trainsToSaveOrUpdate.add(existingTrainInDb);
                            updatedRecords++;
                            log.info("Row {}: Train {} data updated. Added to save/update list.", rowNum + 1, currentTrainNumberStr);
                        } else {
                            log.debug("Row {}: Train {} found in database, no changes detected.", rowNum + 1, currentTrainNumberStr);
                        }
                    } else {
                        trainsToSaveOrUpdate.add(trainScheduleFromExcel);
                        successfullyParsedNew++;
                        log.info("Row {}: New train {} successfully parsed. Added to save/update list.", rowNum + 1, currentTrainNumberStr);
                    }

                } catch (TrainParsingException e) {
                    recordsWithError++;
                    errorMessages.add("Error parsing row " + (rowNum + 1) + ": " + e.getMessage());
                    log.warn("Parsing error in row {}: {}", rowNum + 1, e.getMessage());
                } catch (Exception e) {
                    recordsWithError++;
                    errorMessages.add("Unexpected error processing row " + (rowNum + 1) + ": " + e.getMessage());
                    log.error("Unexpected error in row {}: {}", rowNum + 1, e.getMessage(), e);
                }
            }
        }

        if (!trainsToSaveOrUpdate.isEmpty()) {
            log.info("Saving/updating {} train schedules to the database.", trainsToSaveOrUpdate.size());
            trainScheduleRepository.saveAll(trainsToSaveOrUpdate);
            log.info("Finished saving/updating train schedules.");
        } else {
            log.info("No train schedules to save or update.");
        }


        if (!pairedTrainNumbersMap.isEmpty()) {
            log.info("Starting to process paired train links for {} entries.", pairedTrainNumbersMap.size());
            List<TrainSchedule> relatedTrains = new ArrayList<>();
            if (!allRelatedTrainNumbers.isEmpty()) {
                log.debug("Loading all related trains from database for linking ({} unique train numbers).", allRelatedTrainNumbers.size());
                relatedTrains = trainScheduleRepository.findByTrainNumberIn(allRelatedTrainNumbers);
                log.debug("Loaded {} related trains.", relatedTrains.size());
            }

            Map<String, TrainSchedule> allTrainsForLinking = new HashMap<>();
            relatedTrains.forEach(train -> allTrainsForLinking.put(train.getTrainNumber(), train));
            log.debug("Created map of all related trains for quick lookup.");

            List<TrainSchedule> trainsWithUpdatedPairedLinks = new ArrayList<>();

            for (Map.Entry<String, String> entry : pairedTrainNumbersMap.entrySet()) {
                String currentTrainNumber = entry.getKey();
                String pairedTrainNumber = entry.getValue();
                log.trace("Processing link: current train {} to paired train {}.", currentTrainNumber, pairedTrainNumber);


                TrainSchedule currentTrain = allTrainsForLinking.get(currentTrainNumber);
                TrainSchedule pairedTrain = allTrainsForLinking.get(pairedTrainNumber);

                if (currentTrain != null) {
                    String currentPairedTrainNumberInDb = (currentTrain.getPairTrain() != null) ? currentTrain.getPairTrain().getTrainNumber() : null;

                    if (!Objects.equals(currentPairedTrainNumberInDb, pairedTrainNumber)) {
                        if (pairedTrain != null) {
                            currentTrain.setPairTrain(pairedTrain);
                            trainsWithUpdatedPairedLinks.add(currentTrain);
                            log.info("Updated paired link for train {}. New pair: {}.", currentTrainNumber, pairedTrainNumber);
                        } else {
                            errorMessages.add("Couldn't establish connection for train " + currentTrainNumber + ": paired train " + pairedTrainNumber + " not found in the database.");
                            recordsWithError++;
                            log.warn("Paired train {} for train {} not found in DB. Link not established.", pairedTrainNumber, currentTrainNumber);
                        }
                    } else {
                        log.debug("Paired link for train {} already correct ({}). No update needed.", currentTrainNumber, pairedTrainNumber);
                    }
                } else {
                    errorMessages.add("Train " + currentTrainNumber + " was not found in the database for linking to paired train " + pairedTrainNumber + ".");
                    recordsWithError++;
                    log.warn("Train {} not found in DB for linking with paired train {}.", currentTrainNumber, pairedTrainNumber);
                }
            }
            if (!trainsWithUpdatedPairedLinks.isEmpty()) {
                log.info("Saving {} train schedules with updated paired links.", trainsWithUpdatedPairedLinks.size());
                trainScheduleRepository.saveAll(trainsWithUpdatedPairedLinks);
            } else {
                log.info("No paired links needed to be updated.");
            }
        } else {
            log.info("No paired train numbers found in the Excel file to process.");
        }

        ParsingStatsDto stats = ParsingStatsDto.builder()
                .totalRecords(totalRecords)
                .successfullyParsed(successfullyParsedNew)
                .recordsWithError(recordsWithError)
                .existingRecordsInDb(existingRecordsInDbFound)
                .updatedRecords(updatedRecords)
                .errorMessages(errorMessages)
                .build();
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
}