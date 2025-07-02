package com.pipemasters.server.dto.request.create;

import java.time.LocalDate;

public class UploadBatchCreateDto {
    private Long uploadedById;
    private LocalDate trainDeparted;
    private LocalDate trainArrived;
    private Long trainId;
    private String comment;
    private Long branchId;
    private Long absenceId;

    public UploadBatchCreateDto(Long absenceId, Long branchId, String comment, LocalDate trainArrived, LocalDate trainDeparted, Long trainId, Long uploadedById) {
        this.absenceId = absenceId;
        this.branchId = branchId;
        this.comment = comment;
        this.trainArrived = trainArrived;
        this.trainDeparted = trainDeparted;
        this.trainId = trainId;
        this.uploadedById = uploadedById;
    }

    public Long getAbsenceId() {
        return absenceId;
    }

    public void setAbsenceId(Long absenceId) {
        this.absenceId = absenceId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }
}
