package com.pipemasters.server.dto;

import com.pipemasters.server.entity.enums.FileType;

public class FileUploadRequestDTO {
    private Long uploadBatchId;
    private String filename;
    private FileType fileType;

    public FileUploadRequestDTO() {}

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
}
