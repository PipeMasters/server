package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.FileType;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "upload_batches", indexes = {@Index(columnList = "directory"), @Index(columnList = "branch_id"), @Index(columnList = "train_departed")})
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

    /* дата прибытия поезда (без времени) */
    @Column(nullable = false)
    private LocalDate trainArrived;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @Column(length = 1024)
    private String comment;

    /* ключевые слова для полнотекстового поиска */
//    @ElementCollection
//    @CollectionTable(name = "upload_batch_keywords", joinColumns = @JoinColumn(name = "upload_batch_id"))
//    @Column(name = "keyword")
//    private Set<String> keywords = new HashSet<>();

    /* филиал записи — для разграничения доступа */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /* сроки хранения */
    @Column(nullable = false)
    private boolean archived = false;
    private Instant deletedAt;
    private boolean deleted = false;

    /* файлы, каскад + orphanRemoval */
    @OneToMany(mappedBy = "uploadBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> files = new ArrayList<>();

    @OneToOne(mappedBy = "uploadBatch",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private VideoAbsence absence;


    public UploadBatch(UUID directory, User uploadedBy, Instant createdAt, LocalDate trainDeparted, Train train, String comment, Branch branch, boolean archived, Instant deletedAt, boolean deleted, List<MediaFile> files) {
        this.directory = directory;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.train = train;
        this.comment = comment;
//        this.keywords = keywords;
        this.branch = branch;
        this.archived = archived;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.files = files;
    }

    public UploadBatch(User uploadedBy, LocalDate trainDeparted, LocalDate trainArrived, Train train, String comment, Branch branch) {
        this.directory = UUID.randomUUID();
        this.uploadedBy = uploadedBy;
        this.trainDeparted = trainDeparted;
        this.trainArrived = trainArrived;
        this.train = train;
        this.comment = comment;
        this.branch = branch;
    }

    public UploadBatch() {
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

//    public Set<String> getKeywords() {
//        return keywords;
//    }
//
//    public void setKeywords(Set<String> keywords) {
//        this.keywords = keywords;
//    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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

    public VideoAbsence getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsence absence) {
        this.absence = absence;
    }

    public LocalDate getTrainArrived() {
        return trainArrived;
    }

    public void setTrainArrived(LocalDate trainArrived) {
        this.trainArrived = trainArrived;
    }

    public List<MediaFile> getChainedFiles() {
        return files.stream()
                .filter(file -> file.getFileType() == FileType.VIDEO)
                .filter(file -> {
                    String filename = file.getFilename();
                    int underscoreIndex = filename.lastIndexOf('_');
                    int dotIndex = filename.lastIndexOf('.');
                    if (underscoreIndex == -1 || dotIndex == -1 || underscoreIndex >= dotIndex) return false;
                    String postfix = filename.substring(underscoreIndex + 1, dotIndex);
                    try {
                        Integer.parseInt(postfix);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(file -> {
                    String filename = file.getFilename();
                    int underscoreIndex = filename.lastIndexOf('_');
                    int dotIndex = filename.lastIndexOf('.');
                    String postfix = filename.substring(underscoreIndex + 1, dotIndex);
                    return Integer.parseInt(postfix);
                }))
                .toList();
    }
}