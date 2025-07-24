package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.TagDefinitionResponseDto;
import com.pipemasters.server.entity.MediaFile;

import java.util.List;

public interface TagService {
    List<TagDefinitionResponseDto> getAllUniqueTagNames();
    void fetchAndProcessImotioTags(MediaFile mediaFile, String callId);
}
