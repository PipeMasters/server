package com.pipemasters.server.dto.response;

import java.util.List;

public class MediaFileFragmentsDto {
    private Long mediafileId;
    private List<Long> fragmentsIds;

    public MediaFileFragmentsDto() {
    }

    public MediaFileFragmentsDto(Long mediafileId, List<Long> fragmentsIds) {
        this.mediafileId = mediafileId;
        this.fragmentsIds = fragmentsIds;
    }

    public Long getMediafileId() {
        return mediafileId;
    }

    public void setMediafileId(Long mediafileId) {
        this.mediafileId = mediafileId;
    }

    public List<Long> getFragmentsIds() {
        return fragmentsIds;
    }

    public void setFragmentsIds(List<Long> fragmentsIds) {
        this.fragmentsIds = fragmentsIds;
    }
}