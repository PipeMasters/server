package com.pipemasters.server.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "upload_batches", indexes = {@Index(columnList = "directory"), @Index(columnList = "branch_id"), @Index(columnList = "trainDeparted")})
public class UploadBatch extends BaseEntity {

    /* UUID папки в S3 */
    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID directory;

    /* кто выгрузил */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    /* время загрузки */
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /* дата отправления поезда (без времени) */
    @Column(nullable = false)
    private LocalDate trainDeparted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @Column(length = 1024)
    private String comment;

    /* ключевые слова для полнотекстового поиска */
    @ElementCollection
    @CollectionTable(name = "upload_batch_keywords", joinColumns = @JoinColumn(name = "upload_batch_id"))
    @Column(name = "keyword")
    private Set<String> keywords = new HashSet<>();

    /* филиал записи — для разграничения доступа */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /* сроки хранения */
    private Instant deletedAt;
    private boolean deleted;

    /* файлы, каскад + orphanRemoval */
    @OneToMany(mappedBy = "uploadBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> files = new ArrayList<>();

    @OneToOne(mappedBy = "uploadBatch",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private VideoAbsence absence;


    public UploadBatch(UUID directory, User uploadedBy, Instant createdAt, LocalDate trainDeparted, Train train, String comment, Set<String> keywords, Branch branch, Instant deletedAt, boolean deleted, List<MediaFile> files) {
        this.directory = directory;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.train = train;
        this.comment = comment;
        this.keywords = keywords;
        this.branch = branch;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.files = files;
    }

    protected UploadBatch() {
    }

    public UUID getDirectory() {
        return directory;
    }

    public void setDirectory(UUID directory) {
        this.directory = directory;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getTrainDeparted() {
        return trainDeparted;
    }

    public void setTrainDeparted(LocalDate trainDeparted) {
        this.trainDeparted = trainDeparted;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<MediaFile> getFiles() {
        return files;
    }

    public void setFiles(List<MediaFile> files) {
        this.files = files;
    }

}