package com.pipemasters.server.dto;

import java.time.Instant;
import java.time.LocalDate;

public class UploadBatchDtoMediumResponse {
    private Long id;
    private String uploadedBy;
    private Long trainNumber;
    private LocalDate dateDeparted;
    private LocalDate dateArrived;
    private Instant createdAt;
    private String chiefName;
    private String comment;
    private boolean archived;
    private boolean deleted;
    private VideoAbsenceDto absence;
    private String branchName;

    public UploadBatchDtoMediumResponse() {
    }

    public UploadBatchDtoMediumResponse(Long id, String uploadedBy, Long trainNumber, LocalDate dateDeparted, LocalDate dateArrived, Instant createdAt, String chiefName, String comment, boolean archived, boolean deleted, VideoAbsenceDto absence, String branchName) {
        this.id = id;
        this.uploadedBy = uploadedBy;
        this.trainNumber = trainNumber;
        this.dateDeparted = dateDeparted;
        this.dateArrived = dateArrived;
        this.createdAt = createdAt;
        this.chiefName = chiefName;
        this.comment = comment;
        this.archived = archived;
        this.deleted = deleted;
        this.absence = absence;
        this.branchName = branchName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Long getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Long trainNumber) {
        this.trainNumber = trainNumber;
    }

    public LocalDate getDateDeparted() {
        return dateDeparted;
    }

    public void setDateDeparted(LocalDate dateDeparted) {
        this.dateDeparted = dateDeparted;
    }

    public LocalDate getDateArrived() {
        return dateArrived;
    }

    public void setDateArrived(LocalDate dateArrived) {
        this.dateArrived = dateArrived;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getChiefName() {
        return chiefName;
    }

    public void setChiefName(String chiefName) {
        this.chiefName = chiefName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public VideoAbsenceDto getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsenceDto absence) {
        this.absence = absence;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return "UploadBatchDtoMediumResponse{" +
                "id=" + id +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", trainNumber=" + trainNumber +
                ", dateDeparted=" + dateDeparted +
                ", dateArrived=" + dateArrived +
                ", createdAt=" + createdAt +
                ", chiefName='" + chiefName + '\'' +
                ", comment='" + comment + '\'' +
                ", archived=" + archived +
                ", deleted=" + deleted +
                ", absence=" + absence +
                ", branchName='" + branchName + '\'' +
                '}';
    }
}
