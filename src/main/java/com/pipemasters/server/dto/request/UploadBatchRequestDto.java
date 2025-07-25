package com.pipemasters.server.dto.request;

import com.pipemasters.server.dto.BaseDto;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class UploadBatchRequestDto extends BaseDto {
    @NotNull(message = "Directory cannot be empty")
    private String directory;
    @NotNull(message = "UploadedBy cannot be empty")
    private Long uploadedByUserId;
    @NotNull(message = "CreatedAt cannot be empty")
    private Instant createdAt;
    @NotNull(message = "TrainDeparted cannot be empty")
    private LocalDate trainDeparted;
    @NotNull(message = "Train cannot be empty")
    private Long trainId;
    private String comment;
    private Set<String> keywords = new HashSet<>();
    @NotNull(message = "Branch cannot be empty")
    private Long branchId;
    private boolean archived;
    private Instant deletedAt;
    private boolean deleted;
    private List<MediaFileRequestDto> files = new ArrayList<>();
    private Long absenceId;

    public UploadBatchRequestDto() {
    }

    public UploadBatchRequestDto(String directory, Long uploadedByUserId, Instant createdAt, LocalDate trainDeparted,
                                 Long trainId, String comment, Set<String> keywords, Long branchId, boolean archived,
                                 Instant deletedAt, boolean deleted, List<MediaFileRequestDto> files, Long absenceId) {
        this.directory = directory;
        this.uploadedByUserId = uploadedByUserId;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.trainId = trainId;
        this.comment = comment;
        this.keywords = keywords;
        this.branchId = branchId;
        this.archived = archived;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.files = files;
        this.absenceId = absenceId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Long getUploadedById() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
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

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
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

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
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

    public List<MediaFileRequestDto> getFiles() {
        return files;
    }

    public void setFiles(List<MediaFileRequestDto> files) {
        this.files = files;
    }

    public Long getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Long absenceId) {
        this.absenceId = absenceId;
    }
}
