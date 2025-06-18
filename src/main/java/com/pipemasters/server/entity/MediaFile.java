package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.FileType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "media_files",
        indexes = {@Index(columnList = "record_id")})
public class MediaFile extends BaseEntity {

    @Column(nullable = false, length = 512)
    private String filename;               // ключ в S3

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FileType fileType;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    /* video -> audio; ссылка на исходный файл-«родителя» */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private MediaFile source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id")
    private Record record;

    protected MediaFile() {
    }

    public MediaFile(String filename, FileType fileType, Instant uploadedAt, MediaFile source, Record record) {
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

    public MediaFile getSource() {
        return source;
    }

    public void setSource(MediaFile source) {
        this.source = source;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}