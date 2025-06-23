package com.pipemasters.server.service.impl;

import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.MediaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class MediaFileServiceImpl implements MediaFileService {
    private final Logger log = LoggerFactory.getLogger(MediaFileServiceImpl.class);
    private final MediaFileRepository mediaFileRepository;

    public MediaFileServiceImpl(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    @Transactional
    public void handleMinioFileDeletion(UUID uploadBatchDirectory, String filename) {
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, uploadBatchDirectory);

        if (mediaFileOptional.isPresent()) {
            MediaFile file = mediaFileOptional.get();
            mediaFileRepository.deleteById(file.getId());
            log.info("MediaFile with ID {} (filename: '{}' in batchDirectory '{}') deleted after Minio object removal.", file.getId(), filename, uploadBatchDirectory);
        } else {
            log.warn("MediaFile not found for filename '{}' in batchDirectory '{}'. It might have been already deleted or never existed in DB.", filename, uploadBatchDirectory);
        }
    }
}
