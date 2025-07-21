package com.pipemasters.server.dto.request;

import com.pipemasters.server.entity.enums.FileType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public class FileUploadRequestDto {
    @NotNull(message = "Upload batch ID cannot be empty")
    private Long uploadBatchId;
    @NotBlank(message = "Filename cannot be empty")
    private String filename;
    @NotNull(message = "File type cannot be empty")
    private FileType fileType;
    private Long sourceId; // Optional, for audio extraction
    @NotNull(message = "duration cannot be empty")
    private Duration duration;
    @NotNull(message = "size cannot be empty")
    private Long size;
    @NotNull(message = "hash cannot be empty")
    private String hash;

    public FileUploadRequestDto() {
    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
    }

//    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType, Long sourceId) {
//        this.uploadBatchId = uploadBatchId;
//        this.filename = filename;
//        this.fileType = fileType;
//        this.sourceId = sourceId;
//    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType, Long sourceId, Duration duration, Long size, String hash) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
        this.sourceId = sourceId;
        this.duration = duration;
        this.size = size;
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

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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
