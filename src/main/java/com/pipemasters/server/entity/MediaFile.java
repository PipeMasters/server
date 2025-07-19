package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
    @Table(name = "media_files",
            indexes = {@Index(columnList = "upload_batch_id"), @Index(columnList = "filename")})
    public class MediaFile extends BaseEntity {

        @Column(nullable = false, length = 512)
        private String filename;               // ключ в S3

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 16)
        private FileType fileType;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 16)
        private MediaFileStatus status = MediaFileStatus.PENDING;

        @Column(nullable = false, updatable = false)
        private Instant uploadedAt = Instant.now();

        /* video -> audio; ссылка на исходный файл-«родителя» */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "source_id")
        private com.pipemasters.server.entity.MediaFile source;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "upload_batch_id")
        private UploadBatch uploadBatch;

        private String imotioId;

    public MediaFile(String filename, FileType fileType, UploadBatch uploadBatch) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadBatch = uploadBatch;
    }

    public MediaFile(String filename, FileType fileType, Instant uploadedAt, MediaFile source, UploadBatch uploadBatch) {
        this.filename = filename;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.uploadBatch = uploadBatch;
    }

    public MediaFile() {
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

    public MediaFile getSource() {
        return source;
    }

    public void setSource(MediaFile source) {
        this.source = source;
    }

    public UploadBatch getUploadBatch() {
        return uploadBatch;
    }

    public void setUploadBatch(UploadBatch uploadBatch) {
        this.uploadBatch = uploadBatch;
    }

    public MediaFileStatus getStatus() {
        return status;
    }

    public void setStatus(MediaFileStatus status) {
        this.status = status;
    }

    public String getImotioId() {
        return imotioId;
    }

    public void setImotioId(String imotioId) {
        this.imotioId = imotioId;
    }
}