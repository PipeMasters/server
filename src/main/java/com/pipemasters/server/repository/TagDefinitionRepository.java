package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TagDefinition;
import com.pipemasters.server.entity.enums.TagType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagDefinitionRepository extends GeneralRepository<TagDefinition, Long> {
    Optional<TagDefinition> findByNameAndType(String name, TagType type);
}