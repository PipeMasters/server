package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;
import com.pipemasters.server.entity.enums.TagType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagInstanceResponseDto extends BaseDto {
    private String tagName;
    private TagType tagType;
    private String tagValue;
    private Long fragmentId;
    private Long beginTime;
    private Long endTime;
    private String matchText;

    public TagInstanceResponseDto() {
    }

    public TagInstanceResponseDto(String tagName, TagType tagType, String tagValue, Long fragmentId, Long beginTime, Long endTime, String matchText) {
        this.tagName = tagName;
        this.tagType = tagType;
        this.tagValue = tagValue;
        this.fragmentId = fragmentId;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.matchText = matchText;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public Long getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(Long fragmentId) {
        this.fragmentId = fragmentId;
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
}

