package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;
import com.pipemasters.server.entity.enums.FileType;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaFileResponseDto extends BaseDto {
    private String filename;
    private FileType fileType;
    private Instant uploadedAt;
    private MediaFileResponseDto source;
    private Long duration;
    private Long size;
    private String hash;
    private List<TagInstanceResponseDto> tagInstances;

    public MediaFileResponseDto() {
    }

    public MediaFileResponseDto(String filename, FileType fileType, Instant uploadedAt, MediaFileResponseDto source, Long duration, Long size, String hash) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.duration = duration;
        this.size = size;
        this.hash = hash;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
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

    public List<TagInstanceResponseDto> getTagInstances() {
        return tagInstances;
    }

    public void setTagInstances(List<TagInstanceResponseDto> tagInstances) {
        this.tagInstances = tagInstances;
    }
}
