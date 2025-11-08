package com.pipemasters.server.dto.request.update;

import com.pipemasters.server.dto.request.create.UploadBatchCreateDto;
import com.pipemasters.server.entity.enums.AbsenceCause;

import java.time.LocalDate;

public class UploadBatchUpdateDto {
    private Long uploadedById;
    private LocalDate trainDeparted;
    private LocalDate trainArrived;
    private Long trainId;
    private String comment;
    private Long branchId;
    private UploadBatchCreateDto.VideoAbsenceCreateDto absence;

    public UploadBatchUpdateDto(Long uploadedById, LocalDate trainDeparted, LocalDate trainArrived, Long trainId, String comment, Long branchId, UploadBatchCreateDto.VideoAbsenceCreateDto absence) {
        this.uploadedById = uploadedById;
        this.trainDeparted = trainDeparted;
        this.trainArrived = trainArrived;
        this.trainId = trainId;
        this.comment = comment;
        this.branchId = branchId;
        this.absence = absence;
    }

    public UploadBatchUpdateDto() {
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public LocalDate getTrainDeparted() {
        return trainDeparted;
    }

    public void setTrainDeparted(LocalDate trainDeparted) {
        this.trainDeparted = trainDeparted;
    }

    public LocalDate getTrainArrived() {
        return trainArrived;
    }

    public void setTrainArrived(LocalDate trainArrived) {
        this.trainArrived = trainArrived;
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

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public UploadBatchCreateDto.VideoAbsenceCreateDto getAbsence() {
        return absence;
    }

    public void setAbsence(UploadBatchCreateDto.VideoAbsenceCreateDto absence) {
        this.absence = absence;
    }

    public static class VideoAbsenceUpdateDto {
        private AbsenceCause cause;
        private String comment;

        public VideoAbsenceUpdateDto(String comment, AbsenceCause cause) {
            this.comment = comment;
            this.cause = cause;
        }

        public VideoAbsenceUpdateDto() {
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
