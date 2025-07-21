package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

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
    private MediaFile source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_batch_id")
    private UploadBatch uploadBatch;

    @Column
    private Long duration;

    @Column
    private Long size;
    
    @Column
    private String hash;

    @OneToMany(mappedBy = "mediaFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TranscriptFragment> transcriptFragments;

    @Column
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

    public MediaFile(String filename, FileType fileType, MediaFileStatus status, Instant uploadedAt, MediaFile source, UploadBatch uploadBatch, Long duration, Long size, String hash) {
        this.filename = filename;
        this.fileType = fileType;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.uploadBatch = uploadBatch;
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
    public List<TranscriptFragment> getTranscriptFragments() {
        return transcriptFragments;
    }

    public void setTranscriptFragments(List<TranscriptFragment> transcriptFragments) {
        this.transcriptFragments = transcriptFragments;
    }

    public String getImotioId() {
        return imotioId;
    }

    public void setImotioId(String imotioId) {
        this.imotioId = imotioId;
    }
}