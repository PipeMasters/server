package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.enums.FileType;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaFileResponseDto extends BaseDto{
    private String filename;
    private FileType fileType;
    private Instant uploadedAt;
    private MediaFileResponseDto source;

    public MediaFileResponseDto() {
    }

    public MediaFileResponseDto(String filename, FileType fileType, Instant uploadedAt, MediaFileResponseDto source) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.source = source;
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

    public MediaFileResponseDto getSource() {
        return source;
    }

    public void setSource(MediaFileResponseDto source) {
        this.source = source;
    }
}
