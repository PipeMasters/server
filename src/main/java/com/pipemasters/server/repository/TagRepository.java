package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Tag;
import com.pipemasters.server.entity.TranscriptFragment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends GeneralRepository<Tag, Long> {
    @Query("SELECT t FROM Tag t WHERE t.name = :name AND t.value = :value")
    Optional<Tag> findByNameAndValue(String name, String value);

    @Query("SELECT DISTINCT t.name FROM Tag t")
    List<String> findDistinctNames();
}
