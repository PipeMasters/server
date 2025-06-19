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
    @NotNull(message = "Source cannot be empty")
    private MediaFileDto source;
    private RecordDto record;

    public MediaFileDto() {
    }

    public MediaFileDto( String filename, FileType fileType, Instant uploadedAt, MediaFileDto source, RecordDto record) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.record = record;
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

    public RecordDto getRecord() {
        return record;
    }

    public void setRecord(RecordDto record) {
        this.record = record;
    }
}
