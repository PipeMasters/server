package com.pipemasters.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tag_instances")
public class TagInstance extends BaseEntity {

    @Column(name = "begin_time", nullable = false)
    private Long beginTime;

    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @Column(name = "match_text", length = 256)
    private String matchText;

    @Column(length = 256)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private TagDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transcript_fragment_id", nullable = false)
    private TranscriptFragment fragment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    protected TagInstance() {
    }

    public TagInstance(Long beginTime, Long endTime, String matchText, String value, TagDefinition definition, TranscriptFragment fragment, MediaFile mediaFile) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.matchText = matchText;
        this.value = value;
        this.definition = definition;
        this.fragment = fragment;
        this.mediaFile = mediaFile;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getMatchText() {
        return matchText;
    }

    public void setMatchText(String matchText) {
        this.matchText = matchText;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TagDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(TagDefinition definition) {
        this.definition = definition;
    }

    public TranscriptFragment getFragment() {
        return fragment;
    }

    public void setFragment(TranscriptFragment fragment) {
        this.fragment = fragment;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }
}
