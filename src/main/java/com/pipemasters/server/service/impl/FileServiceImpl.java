package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.exceptions.file.FileAlreadyExistsException;
import com.pipemasters.server.exceptions.file.FileGenerationException;
import com.pipemasters.server.exceptions.file.UploadBatchNotFoundException;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String minioBucketName;
    private final MediaFileRepository mediaFileRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public FileServiceImpl(S3Presigner s3Presigner, S3Client s3Client, String minioBucketName, MediaFileRepository mediaFileRepository, UploadBatchRepository uploadBatchRepository) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.minioBucketName = minioBucketName;
        this.mediaFileRepository = mediaFileRepository;
        this.uploadBatchRepository = uploadBatchRepository;
    }

    @Override
    @Transactional
    public String generatePresignedUploadUrl(FileUploadRequestDto fileUploadRequestDTO) {
        UploadBatch uploadBatch = uploadBatchRepository.findById(fileUploadRequestDTO.getUploadBatchId())
                .orElseThrow(() -> new UploadBatchNotFoundException("UploadBatch not found with ID: " + fileUploadRequestDTO.getUploadBatchId()));

        String s3Key = fileUploadRequestDTO.getFilename();
        String fullS3Path = uploadBatch.getDirectory() + "/" + s3Key;
        logger.debug("Generated S3 key for uploadUrl: {}", uploadBatch.getDirectory() + "/" + s3Key);

        Optional<MediaFile> existingMediaFileOptional = mediaFileRepository.findByFilenameAndUploadBatchDirectory(
                fileUploadRequestDTO.getFilename(), uploadBatch.getDirectory());

        MediaFile mediaFile;

        if (existingMediaFileOptional.isPresent()) {
            mediaFile = existingMediaFileOptional.get();

            if (mediaFile.getStatus() != MediaFileStatus.PENDING) {
                throw new FileAlreadyExistsException("File with the name " + fileUploadRequestDTO.getFilename() + " already exists.");
            }
            logger.info("Found existing MediaFile with PENDING status. Re-generating upload URL for file: {}", fullS3Path);
        } else {
            mediaFile = new MediaFile();
            mediaFile.setFilename(s3Key);
            mediaFile.setFileType(fileUploadRequestDTO.getFileType());
            mediaFile.setUploadBatch(uploadBatch);
            mediaFile.setStatus(MediaFileStatus.PENDING);
        }

        if (fileUploadRequestDTO.getSourceId() != null) {
            mediaFile.setSource(mediaFileRepository.findById(fileUploadRequestDTO.getSourceId()).orElseThrow(() -> new MediaFileNotFoundException("Source MediaFile not found with ID: " + fileUploadRequestDTO.getSourceId())));
        }

        mediaFileRepository.save(mediaFile);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(minioBucketName)
                    .key(fullS3Path)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            logger.error("Failed to generate presigned upload URL for file: {}", s3Key, e);
            throw new FileGenerationException("Failed to generate presigned upload URL for file: " + s3Key, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generatePresignedDownloadUrl(Long mediaFileId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new MediaFileNotFoundException("MediaFile not found with ID: " + mediaFileId));

        String s3Key = mediaFile.getUploadBatch().getDirectory() + "/" + mediaFile.getFilename();
        logger.debug("Generated S3 key for downloadUrl: {}", s3Key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(minioBucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            logger.error("Failed to generate presigned download URL for mediaFileId: {}", mediaFileId, e);
            throw new FileGenerationException("Failed to generate presigned download URL for mediaFileId: " + mediaFileId, e);
        }
    }

    @Override
    public void deleteUploadBatchDirectory(UUID directoryUuid) {
        try {
            List<ObjectIdentifier> objectsToDelete = new LinkedList<>();
            String prefix = directoryUuid.toString() + "/";

            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(minioBucketName)
                    .prefix(prefix)
                    .build();

            s3Client.listObjectsV2Paginator(listObjectsRequest)
                    .contents()
                    .forEach(s3Object -> objectsToDelete.add(ObjectIdentifier.builder().key(s3Object.key()).build()));


            if (!objectsToDelete.isEmpty()) {
                DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                        .bucket(minioBucketName)
                        .delete(Delete.builder()
                                .objects(objectsToDelete)
                                .build())
                        .build();

                s3Client.deleteObjects(deleteObjectsRequest);
                logger.info("Successfully deleted {} objects from MinIO for directory: {}", objectsToDelete.size(), directoryUuid);
            } else {
                logger.info("No objects found to delete in MinIO for directory: {}", directoryUuid);
            }

        } catch (Exception e) {
            logger.error("Failed to delete directory {} from MinIO. Error: {}", directoryUuid, e.getMessage(), e);
            throw new FileGenerationException("Failed to delete directory from MinIO: " + directoryUuid, e);
        }
    }
}