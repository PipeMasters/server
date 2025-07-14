package com.pipemasters.server.dto;

import java.util.List;

public class ParsingStatsDto {
    private int totalRecords;
    private int successfullyParsed;
    private int recordsWithError;
    private int existingRecordsInDb;
    private int updatedRecords;
    private List<String> errorMessages;

    public ParsingStatsDto(int totalRecords, int successfullyParsed, int recordsWithError, int existingRecordsInDb, int updatedRecords, List<String> errorMessages) {
        this.totalRecords = totalRecords;
        this.successfullyParsed = successfullyParsed;
        this.recordsWithError = recordsWithError;
        this.existingRecordsInDb = existingRecordsInDb;
        this.updatedRecords = updatedRecords;
        this.errorMessages = errorMessages;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessfullyParsed() {
        return successfullyParsed;
    }

    public void setSuccessfullyParsed(int successfullyParsed) {
        this.successfullyParsed = successfullyParsed;
    }

    public int getRecordsWithError() {
        return recordsWithError;
    }

    public void setRecordsWithError(int recordsWithError) {
        this.recordsWithError = recordsWithError;
    }

    public int getExistingRecordsInDb() {
        return existingRecordsInDb;
    }

    public void setExistingRecordsInDb(int existingRecordsInDb) {
        this.existingRecordsInDb = existingRecordsInDb;
    }

    public int getUpdatedRecords() {
        return updatedRecords;
    }

    public void setUpdatedRecords(int updatedRecords) {
        this.updatedRecords = updatedRecords;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public String toString() {
        return "ParsingStatsDto{" +
                "totalRecords=" + totalRecords +
                ", successfullyParsed=" + successfullyParsed +
                ", recordsWithError=" + recordsWithError +
                ", existingRecordsInDb=" + existingRecordsInDb +
                ", updatedRecords=" + updatedRecords +
                ", errorMessages=" + errorMessages +
                '}';
    }
}
