package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TagDefinition;
import com.pipemasters.server.entity.enums.TagType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagDefinitionRepository extends GeneralRepository<TagDefinition, Long> {
    Optional<TagDefinition> findByNameAndType(String name, TagType type);

    @Query("SELECT DISTINCT td.name FROM TagDefinition td")
    List<String> findDistinctNames();
}