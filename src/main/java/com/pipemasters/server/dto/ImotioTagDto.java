package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImotioTagDto {
    private String name;
    private String value;
    private Boolean visible;
    @JsonProperty("tag_type")
    private String tagType;
    private Long begin;
    private Long end;
    @JsonProperty("match_text")
    private String matchText;
    @JsonProperty("fragment_id")
    private String fragmentId;

    public ImotioTagDto(String name, String value, Boolean visible, String tagType, Long begin, Long end, String matchText, String fragmentId) {
        this.name = name;
        this.value = value;
        this.visible = visible;
        this.tagType = tagType;
        this.begin = begin;
        this.end = end;
        this.matchText = matchText;
        this.fragmentId = fragmentId;
    }

    public ImotioTagDto() {
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

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getMatchText() {
        return matchText;
    }

    public void setMatchText(String matchText) {
        this.matchText = matchText;
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(String fragmentId) {
        this.fragmentId = fragmentId;
    }
}
