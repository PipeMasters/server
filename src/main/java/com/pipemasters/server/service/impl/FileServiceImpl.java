package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final S3Presigner s3Presigner;
    private final String minioBucketName;
    private final MediaFileRepository mediaFileRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //    @Autowired
    public FileServiceImpl(S3Presigner s3Presigner, String minioBucketName, MediaFileRepository mediaFileRepository, UploadBatchRepository uploadBatchRepository) {
        this.s3Presigner = s3Presigner;
        this.minioBucketName = minioBucketName;
        this.mediaFileRepository = mediaFileRepository;
        this.uploadBatchRepository = uploadBatchRepository;
    }

    @Override
    @Transactional
    public String generatePresignedUploadUrl(FileUploadRequestDto fileUploadRequestDTO) {
        UploadBatch uploadBatch = uploadBatchRepository.findById(fileUploadRequestDTO.getUploadBatchId())
                .orElseThrow(() -> new RuntimeException("UploadBatch not found with ID: " + fileUploadRequestDTO.getUploadBatchId()));

        String s3Key = fileUploadRequestDTO.getFilename();
        logger.debug("Generated S3 key for uploadUrl: {}", uploadBatch.getDirectory() + "/" + s3Key);
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFilename(s3Key);
        mediaFile.setFileType(fileUploadRequestDTO.getFileType());
        mediaFile.setUploadBatch(uploadBatch);
        mediaFile.setStatus(MediaFileStatus.PENDING);
        if (fileUploadRequestDTO.getSourceId() != null) {
            mediaFile.setSource(mediaFileRepository.findById(fileUploadRequestDTO.getSourceId()).orElseThrow(() -> new RuntimeException("Source MediaFile not found with ID: " + fileUploadRequestDTO.getSourceId())));
        }
        mediaFileRepository.save(mediaFile);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(minioBucketName)
                .key(uploadBatch.getDirectory() + "/" + s3Key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    @Transactional
    public String generatePresignedDownloadUrl(Long mediaFileId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new RuntimeException("MediaFile not found with ID: " + mediaFileId));

        String s3Key = mediaFile.getUploadBatch().getDirectory() + "/" + mediaFile.getFilename();
        logger.debug("Generated S3 key for downloadUrl: {}", s3Key);
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
    }
}