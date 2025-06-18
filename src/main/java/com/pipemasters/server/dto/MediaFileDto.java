package com.pipemasters.server.dto;

import com.pipemasters.server.entity.enums.FileType;

import java.time.Instant;

public class MediaFileDto extends BaseDto{
    private String filename;
    private FileType fileType;
    private Instant uploadedAt;
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
