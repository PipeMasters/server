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
    private MediaFileDto source;
    @NotNull(message = "ToDate cannot be empty")
    private UploadBatchDto uploadBatch;

    public MediaFileDto() {
    }

    public MediaFileDto( String filename, FileType fileType, Instant uploadedAt, MediaFileDto source, UploadBatchDto uploadBatch) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.uploadBatch = uploadBatch;
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

    public MediaFileDto getSource() {
        return source;
    }

    public void setSource(MediaFileDto source) {
        this.source = source;
    }

    public UploadBatchDto getUploadBatch() {
        return uploadBatch;
    }

    public void setUploadBatch(UploadBatchDto uploadBatch) {
        this.uploadBatch = uploadBatch;
    }
}
