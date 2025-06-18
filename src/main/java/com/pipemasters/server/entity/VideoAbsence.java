package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.AbsenceCause;
import jakarta.persistence.*;

@Entity
@Table(name = "video_absences")
public class VideoAbsence extends BaseEntity {

    /* Связь на тот же Record, к которому не хватает видео */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", unique = true)
    private Record record;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AbsenceCause cause;

    /* подробное пояснение */
    @Column(length = 1024)
    private String comment;

    protected VideoAbsence() {
    }

    public VideoAbsence(Record record, AbsenceCause cause, String comment) {
        this.record = record;
        this.cause = cause;
        this.comment = comment;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
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