package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadBatchResponseDto extends BaseDto {
    private String directory;
    private Long uploadedId;
    private Instant createdAt;
    private LocalDate trainDeparted;
    private Long trainId;
    private String comment;
    private Set<String> keywords = new HashSet<>();
    private Long branchId;
    private boolean archived;
    private Instant deletedAt;
    private boolean deleted;
    private MediaFileResponseDto file;
    private Long absenceId;

    public UploadBatchResponseDto() {
    }

    public UploadBatchResponseDto(String directory, Long uploadedId, Instant createdAt, LocalDate trainDeparted, Long trainId, String comment, Set<String> keywords, Long branchId, boolean archived, Instant deletedAt, boolean deleted, MediaFileResponseDto file, Long absenceId) {
        this.directory = directory;
        this.uploadedId = uploadedId;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.trainId = trainId;
        this.comment = comment;
        this.keywords = keywords;
        this.branchId = branchId;
        this.archived = archived;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.file = file;
        this.absenceId = absenceId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Long getUploadedId() {
        return uploadedId;
    }

    public void setUploadedId(Long uploadedId) {
        this.uploadedId = uploadedId;
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

    public MediaFileResponseDto getFile() {
        return file;
    }

    public void setFile(MediaFileResponseDto file) {
        this.file = file;
    }

    public Long getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Long absenceId) {
        this.absenceId = absenceId;
    }
}
