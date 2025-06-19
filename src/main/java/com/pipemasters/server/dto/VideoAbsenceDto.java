package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.enums.AbsenceCause;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoAbsenceDto extends BaseDto{
    @NotNull(message = "UploadBatchDto cannot be empty")
    private UploadBatchDto uploadBatch;
    @NotNull(message = "AbsenceCause cannot be empty")
    private AbsenceCause cause;
//    @NotNull(message = "Comment cannot be empty")
    private String comment;

    public VideoAbsenceDto() {
    }

    public VideoAbsenceDto( UploadBatchDto uploadBatch, AbsenceCause cause, String comment) {
        this.uploadBatch = uploadBatch;
        this.cause = cause;
        this.comment = comment;
    }

    public UploadBatchDto getUploadBatch() {
        return uploadBatch;
    }

    public void setUploadBatch(UploadBatchDto uploadBatch) {
        this.uploadBatch = uploadBatch;
    }

    public AbsenceCause getCause() {
        return cause;
    }

    public void setCause(AbsenceCause cause) {
        this.cause = cause;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
