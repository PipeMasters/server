package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadBatchDto extends BaseDto{
    @NotNull(message = "Directory cannot be empty")
    private String directory;
    @NotNull(message = "UploadedBy cannot be empty")
    private UserDto uploadedBy;
    @NotNull(message = "CreatedAt cannot be empty")
    private Instant createdAt;
    @NotNull(message = "TrainDeparted cannot be empty")
    private LocalDate trainDeparted;
    @NotNull(message = "Train cannot be empty")
    private TrainDto train;
    private String comment;
    private Set<String> keywords = new HashSet<>();
    @NotNull(message = "Branch cannot be empty")
    private BranchDto branch;
    private boolean archived;
    private Instant deletedAt;
    private boolean deleted;
    private List<MediaFileDto> files = new ArrayList<>();
    private VideoAbsenceDto absence;

    public UploadBatchDto() {
    }

    public UploadBatchDto( String directory, UserDto uploadedBy, Instant createdAt, LocalDate trainDeparted,
                     TrainDto train, String comment, Set<String> keywords, BranchDto branch, boolean archived,
                     Instant deletedAt, boolean deleted, List<MediaFileDto> files, VideoAbsenceDto absence) {
        this.directory = directory;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.train = train;
        this.comment = comment;
        this.keywords = keywords;
        this.branch = branch;
        this.archived = archived;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.files = files;
        this.absence = absence;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public UserDto getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserDto uploadedBy) {
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

    public TrainDto getTrain() {
        return train;
    }

    public void setTrain(TrainDto train) {
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

    public BranchDto getBranch() {
        return branch;
    }

    public void setBranch(BranchDto branch) {
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

    public List<MediaFileDto> getFiles() {
        return files;
    }

    public void setFiles(List<MediaFileDto> files) {
        this.files = files;
    }

    public VideoAbsenceDto getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsenceDto absence) {
        this.absence = absence;
    }
}
