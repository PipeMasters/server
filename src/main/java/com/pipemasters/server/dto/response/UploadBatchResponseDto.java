package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;
import com.pipemasters.server.dto.VideoAbsenceDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadBatchResponseDto extends BaseDto {
    private String directory;
    private UserResponseDto uploadedBy;
    private UserResponseDto chief;
    private Instant createdAt;
    private LocalDate trainDeparted;
    private LocalDate trainArrived;
    private Long trainId;
    private String comment;
    private Set<String> keywords = new HashSet<>();
    private Long branchId;
    private boolean archived;
    private Instant deletedAt;
    private boolean deleted;
    private MediaFileResponseDto file;
    private List<MediaFileResponseDto> files;
    private VideoAbsenceDto absence;

    public UploadBatchResponseDto(String directory, UserResponseDto uploadedBy, UserResponseDto chief,
                                  Instant createdAt, LocalDate trainDeparted, LocalDate trainArrived,
                                  Long trainId, String comment, Set<String> keywords, Long branchId,
                                  boolean archived, Instant deletedAt, boolean deleted,
                                  MediaFileResponseDto file, List<MediaFileResponseDto> files,
                                  VideoAbsenceDto absence) {
        this.directory = directory;
        this.uploadedBy = uploadedBy;
        this.chief = chief;
        this.createdAt = createdAt;
        this.trainDeparted = trainDeparted;
        this.trainArrived = trainArrived;
        this.trainId = trainId;
        this.comment = comment;
        this.keywords = keywords;
        this.branchId = branchId;
        this.archived = archived;
        this.deletedAt = deletedAt;
        this.deleted = deleted;
        this.file = file;
        this.files = files;
        this.absence = absence;
    }

    public UploadBatchResponseDto() {
    }

    public VideoAbsenceDto getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsenceDto absence) {
        this.absence = absence;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public UserResponseDto getChief() {
        return chief;
    }

    public void setChief(UserResponseDto chief) {
        this.chief = chief;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public MediaFileResponseDto getFile() {
        return file;
    }

    public void setFile(MediaFileResponseDto file) {
        this.file = file;
    }

    public List<MediaFileResponseDto> getFiles() {
        return files;
    }

    public void setFiles(List<MediaFileResponseDto> files) {
        this.files = files;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public LocalDate getTrainArrived() {
        return trainArrived;
    }

    public void setTrainArrived(LocalDate trainArrived) {
        this.trainArrived = trainArrived;
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

    public UserResponseDto getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserResponseDto uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}

