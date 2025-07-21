package com.pipemasters.server.exceptions;

import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.exceptions.audio.AudioExtractionException;
import com.pipemasters.server.exceptions.branch.InvalidBranchHierarchyException;
import com.pipemasters.server.exceptions.branch.InvalidBranchLevelException;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.exceptions.delegation.DelegationDateValidationException;
import com.pipemasters.server.exceptions.file.*;
import com.pipemasters.server.exceptions.imotio.ImotioApiCallException;
import com.pipemasters.server.exceptions.imotio.ImotioProcessingException;
import com.pipemasters.server.exceptions.imotio.ImotioResponseParseException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNumberExistsException;
import com.pipemasters.server.exceptions.trainSchedule.FileReadException;
import com.pipemasters.server.exceptions.trainSchedule.TrainParsingException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MediaFileNotFoundException.class)
    public ResponseEntity<Object> handleMediaFileNotFoundException(
            MediaFileNotFoundException ex) {
        log.error("MediaFileNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AudioExtractionException.class)
    public ResponseEntity<Object> handleAudioExtractionException(
            AudioExtractionException ex) {
        log.error("AudioExtractionException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An error occurred during audio extraction: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<Object> handleFileDownloadException(
            FileDownloadException ex) {
        log.error("FileDownloadException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Failed to download required file: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Object> handleFileUploadException(
            FileUploadException ex) {
        log.error("FileUploadException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Failed to upload file: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(
            UserNotFoundException ex) {
        log.error("UserNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DelegationDateValidationException.class)
    public ResponseEntity<Object> handleDelegationDateValidationException(
            DelegationDateValidationException ex) {
        log.error("DelegationDateValidationException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UploadBatchNotFoundException.class)
    public ResponseEntity<Object> handleUploadBatchNotFoundException(
            UploadBatchNotFoundException ex) {
        log.error("UploadBatchNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<Object> handleFileAlreadyExistsException(
            FileAlreadyExistsException ex) {
        log.error("FileAlreadyExistsException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.CONFLICT, "Conflict", ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileGenerationException.class)
    public ResponseEntity<Object> handleFileGenerationException(
            FileGenerationException ex) {
        log.error("FileGenerationException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An error occurred while generating file link: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TrainNotFoundException.class)
    public ResponseEntity<Object> handleTrainNotFoundException(
            TrainNotFoundException ex) {
        log.error("TrainNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<Object> handleBranchNotFoundException(
            BranchNotFoundException ex) {
        log.error("BranchNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            RuntimeException ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidBranchHierarchyException.class)
    public ResponseEntity<Object> handleInvalidBranchHierarchyException(
            InvalidBranchHierarchyException ex) {
        log.error("InvalidBranchHierarchyException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidBranchLevelException.class)
    public ResponseEntity<Object> handleInvalidBranchLevelException(
            InvalidBranchLevelException ex) {
        log.error("InvalidBranchLevelException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TrainNumberExistsException.class)
    public ResponseEntity<Object> handleTrainNumberExistsException(
            TrainNumberExistsException ex) {
        log.error("TrainNumberExistsException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<ParsingStatsDto> handleFileReadException(FileReadException ex) {
        log.error("FileReadException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ParsingStatsDto(
                        0,
                        0,
                        1,
                        0,
                        0,
                        Collections.singletonList(ex.getMessage())
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(TrainParsingException.class)
    public ResponseEntity<ParsingStatsDto> handleTrainParsingException(TrainParsingException ex) {
        log.error("TrainParsingException: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ParsingStatsDto(
                        0,
                        0,
                        1,
                        0,
                        0,
                        Collections.singletonList("Data parsing error: " + ex.getMessage())
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidFileKeyException.class)
    public ResponseEntity<Object> handleInvalidFileKeyException(
            InvalidFileKeyException ex) {
        log.error("InvalidFileKeyException: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImotioApiCallException.class)
    public ResponseEntity<Object> handleImotioApiCallException(ImotioApiCallException ex) {
        log.error("ImotioApiCallException: {}. Status Code: {}. Response Body: {}",
                ex.getMessage(), ex.getStatusCode(), ex.getResponseBody(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "External Service Error",
                "Failed to communicate with Imotio API: " + ex.getMessage() + ". Imotio Status: " + ex.getStatusCode()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ImotioResponseParseException.class)
    public ResponseEntity<Object> handleImotioResponseParseException(ImotioResponseParseException ex) {
        log.error("ImotioResponseParseException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "External Service Response Error",
                "Failed to parse Imotio API response: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ImotioProcessingException.class)
    public ResponseEntity<Object> handleImotioProcessingException(ImotioProcessingException ex) {
        log.error("ImotioProcessingException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Imotio Processing Error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
