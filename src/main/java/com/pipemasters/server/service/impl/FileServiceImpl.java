package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.FileService;
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

    @Autowired
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

        String s3Key = minioBucketName + "/" + uploadBatch.getDirectory() + "/" + UUID.randomUUID() + "_" + fileUploadRequestDTO.getFilename();

        MediaFile mediaFile = new MediaFile();
        mediaFile.setFilename(s3Key);
        mediaFile.setFileType(fileUploadRequestDTO.getFileType());
        mediaFile.setUploadBatch(uploadBatch);
        mediaFileRepository.save(mediaFile);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(minioBucketName)
                .key(s3Key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public String generatePresignedDownloadUrl(Long mediaFileId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new RuntimeException("MediaFile not found with ID: " + mediaFileId));

        String s3Key = mediaFile.getFilename();

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