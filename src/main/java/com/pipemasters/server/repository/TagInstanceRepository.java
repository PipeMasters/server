package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TagDefinition;
import com.pipemasters.server.entity.TagInstance;
import com.pipemasters.server.entity.TranscriptFragment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagInstanceRepository extends GeneralRepository<TagInstance, Long> {
    Optional<TagInstance> findByDefinitionAndFragmentAndBeginTimeAndEndTimeAndValue(
            TagDefinition definition,
            TranscriptFragment fragment,
            Long beginTime,
            Long endTime,
            String value
    );
}