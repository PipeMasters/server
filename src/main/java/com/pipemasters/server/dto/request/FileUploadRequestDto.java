package com.pipemasters.server.dto.request;

import com.pipemasters.server.entity.enums.FileType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import java.time.Duration;
import java.time.Instant;

public class FileUploadRequestDto {
    @NotNull(message = "Upload batch ID cannot be empty")
    private Long uploadBatchId;
    @NotBlank(message = "Filename cannot be empty")
    private String filename;
    @NotNull(message = "File type cannot be empty")
    private FileType fileType;
    private Long sourceId; // Optional, for audio extraction
    @NotNull(message = "duration cannot be empty")
    private Long duration;
    @NotNull(message = "hash cannot be empty")
    private String hash;
    @NotNull(message = "createdAt cannot be empty")
    @PastOrPresent(message = "creation time must be in past or present")
    private Instant createdAt;

    public FileUploadRequestDto() {
    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType, Long sourceId, Long duration, String hash) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
        this.sourceId = sourceId;
        this.duration = duration;
        this.hash = hash;
    }

    public Long getUploadBatchId() {
        return uploadBatchId;
    }

    public void setUploadBatchId(Long uploadBatchId) {
        this.uploadBatchId = uploadBatchId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FileUploadRequestDto{" +
                "uploadBatchId=" + uploadBatchId +
                ", filename='" + filename + '\'' +
                ", fileType=" + fileType +
                ", sourceId=" + sourceId +
                '}';
    }
}
