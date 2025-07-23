package com.pipemasters.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "value"})
})
public class Tag extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 256)
    private String value;

    @Column(nullable = false, length = 32)
    private String tagType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_fragment_id", nullable = false)
    private TranscriptFragment transcriptFragment;

    public Tag(String name, String value, String tagType, MediaFile mediaFile, TranscriptFragment transcriptFragment) {
        this.name = name;
        this.value = value;
        this.tagType = tagType;
        this.mediaFile = mediaFile;
        this.transcriptFragment = transcriptFragment;
    }

    protected Tag() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public TranscriptFragment getTranscriptFragment() {
        return transcriptFragment;
    }

    public void setTranscriptFragment(TranscriptFragment transcriptFragment) {
        this.transcriptFragment = transcriptFragment;
    }
}
