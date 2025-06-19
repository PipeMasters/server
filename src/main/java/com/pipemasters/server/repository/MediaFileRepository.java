package com.pipemasters.server.repository;

import com.pipemasters.server.entity.MediaFile;
import org.springframework.data.repository.CrudRepository;

public interface MediaFileRepository extends GeneralRepository<MediaFile, Long> {
    void deleteById(Long id);
}
