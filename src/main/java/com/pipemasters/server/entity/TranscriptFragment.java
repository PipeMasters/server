package com.pipemasters.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transcript_fragments",
        indexes = {@Index(columnList = "media_file_id")})
public class TranscriptFragment extends BaseEntity {

    @Column(nullable = false)
    private Long beginTime;

    @Column(nullable = false)
    private Long endTime;

    @Column(length = 32)
    private String direction;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "fragment_id", length = 64)
    private String fragmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_file_id")
    private MediaFile mediaFile;

    @Column(columnDefinition = "tsvector generated always as (to_tsvector('russian', text)) stored",
            insertable = false,
            updatable = false)
    private String tsv;

    public TranscriptFragment() {
    }

    public TranscriptFragment(Long beginTime, Long endTime, String direction, String text, String fragmentId, MediaFile mediaFile) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.direction = direction;
        this.text = text;
        this.fragmentId = fragmentId;
        this.mediaFile = mediaFile;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long begin) {
        this.beginTime = begin;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long end) {
        this.endTime = end;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(String fragmentId) {
        this.fragmentId = fragmentId;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public String getTsv() {
        return tsv;
    }

    public void setTsv(String tsv) {
        this.tsv = tsv;
    }
}