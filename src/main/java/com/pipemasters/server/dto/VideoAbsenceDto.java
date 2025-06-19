package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.enums.AbsenceCause;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoAbsenceDto extends BaseDto{
    private RecordDto record;
    private AbsenceCause cause;
    private String comment;

    public VideoAbsenceDto() {
    }

    public VideoAbsenceDto( RecordDto record, AbsenceCause cause, String comment) {
        this.record = record;
        this.cause = cause;
        this.comment = comment;
    }

    public RecordDto getRecord() {
        return record;
    }

    public void setRecord(RecordDto record) {
        this.record = record;
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
