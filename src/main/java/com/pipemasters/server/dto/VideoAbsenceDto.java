package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.entity.enums.AbsenceCause;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoAbsenceDto extends BaseDto{
    private AbsenceCause cause;
    private String comment;

    public VideoAbsenceDto() {
    }

    public VideoAbsenceDto(UploadBatchRequestDto uploadBatch, AbsenceCause cause, String comment) {
        this.cause = cause;
        this.comment = comment;
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
