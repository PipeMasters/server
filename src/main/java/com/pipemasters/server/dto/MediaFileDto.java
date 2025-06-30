package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.enums.FileType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaFileDto extends BaseDto{
    @NotNull(message = "FileName cannot be empty")
    private String filename;
    @NotNull(message = "fileType cannot be empty")
    private FileType fileType;
    @NotNull(message = "UploadedAt cannot be empty")
    private Instant uploadedAt;
//    @NotNull(message = "Source cannot be empty")
    private Long sourceId;
    @NotNull(message = "ToDate cannot be empty")
    private Long uploadBatchId;

    public MediaFileDto() {
    }

    public MediaFileDto( String filename, FileType fileType, Instant uploadedAt, Long sourceId, Long uploadBatchId) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.sourceId = sourceId;
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

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getUploadBatchId() {
        return uploadBatchId;
    }

    public void setUploadBatchId(Long uploadBatchId) {
        this.uploadBatchId = uploadBatchId;
    }
}
