package com.pipemasters.server.dto;

import com.pipemasters.server.entity.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class RecordDto extends BaseDto{
    private String directory;
    private UserDto uploadedBy;
    private Instant createdAt;
    private LocalDate trainDeparted;
    private TrainDto train;
    private String comment;
    private Set<String> keywords = new HashSet<>();
    private BranchDto branch;
    private Instant deletedAt;
    private boolean deleted;
    private List<MediaFileDto> files = new ArrayList<>();
    private VideoAbsence absence;

    public RecordDto() {
    }

    public RecordDto(Long id, String directory, UserDto uploadedBy, Instant createdAt, LocalDate trainDeparted,
                     TrainDto train, String comment, Set<String> keywords, BranchDto branch,
                     Instant deletedAt, boolean deleted, List<MediaFileDto> files, VideoAbsence absence) {
        super(id);
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

    public VideoAbsence getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsence absence) {
        this.absence = absence;
    }
}
