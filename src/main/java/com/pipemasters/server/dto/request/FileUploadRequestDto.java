package com.pipemasters.server.dto.request;

import com.pipemasters.server.entity.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FileUploadRequestDto {
    @NotNull(message = "Upload batch ID cannot be empty")
    private Long uploadBatchId;
    @NotBlank(message = "Filename cannot be empty")
    private String filename;
    @NotNull(message = "File type cannot be empty")
    private FileType fileType;
    private Long sourceId; // Optional, for audio extraction

    public FileUploadRequestDto() {
    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
    }

    public FileUploadRequestDto(Long uploadBatchId, String filename, FileType fileType, Long sourceId) {
        this.uploadBatchId = uploadBatchId;
        this.filename = filename;
        this.fileType = fileType;
        this.sourceId = sourceId;
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
