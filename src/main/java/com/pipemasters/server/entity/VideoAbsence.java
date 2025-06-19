package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.AbsenceCause;
import jakarta.persistence.*;

@Entity
@Table(name = "video_absences")
public class VideoAbsence extends BaseEntity {

    /* Связь на тот же UploadBatch, к которому не хватает видео */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_batch_id", unique = true)
    private UploadBatch uploadBatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AbsenceCause cause;

    /* подробное пояснение */
    @Column(length = 1024)
    private String comment;

    public VideoAbsence(UploadBatch uploadBatch, AbsenceCause cause, String comment) {
        this.uploadBatch = uploadBatch;
        this.cause = cause;
        this.comment = comment;
    }

    protected VideoAbsence() {

    }

    public UploadBatch getUploadBatch() {
        return uploadBatch;
    }

    public void setUploadBatch(UploadBatch uploadBatch) {
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