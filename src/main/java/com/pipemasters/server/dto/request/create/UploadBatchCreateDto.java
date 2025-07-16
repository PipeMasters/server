package com.pipemasters.server.dto.request.create;

import com.pipemasters.server.entity.enums.AbsenceCause;

import java.time.LocalDate;

public class UploadBatchCreateDto {
    private Long uploadedById;
    private LocalDate trainDeparted;
    private LocalDate trainArrived;
    private Long trainId;
    private String comment;
    private Long branchId;
    private VideoAbsenceCreateDto absence;

    public UploadBatchCreateDto(VideoAbsenceCreateDto absence, Long branchId, String comment, LocalDate trainArrived, LocalDate trainDeparted, Long trainId, Long uploadedById) {
        this.absence = absence;
        this.branchId = branchId;
        this.comment = comment;
        this.trainArrived = trainArrived;
        this.trainDeparted = trainDeparted;
        this.trainId = trainId;
        this.uploadedById = uploadedById;
    }

    public UploadBatchCreateDto() {
    }

    public VideoAbsenceCreateDto getAbsence() {
        return absence;
    }

    public void setAbsence(VideoAbsenceCreateDto absence) {
        this.absence = absence;
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

    public static class VideoAbsenceCreateDto {
        private AbsenceCause cause;
        private String comment;

        public VideoAbsenceCreateDto(String comment, AbsenceCause cause) {
            this.comment = comment;
            this.cause = cause;
        }

        public VideoAbsenceCreateDto() {
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public AbsenceCause getCause() {
            return cause;
        }

        public void setCause(AbsenceCause cause) {
            this.cause = cause;
        }
    }
}
