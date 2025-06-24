package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.MediaFileResponseDto;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.MediaFileService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileServiceImpl implements MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private final ModelMapper modelMapper;

    public MediaFileServiceImpl(MediaFileRepository mediaFileRepository, ModelMapper modelMapper) {
        this.mediaFileRepository = mediaFileRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<MediaFileResponseDto> getMediaFilesByUploadBatchId(Long uploadBatchId) {
        return mediaFileRepository.findByUploadBatchId(uploadBatchId).stream().map(m ->
                modelMapper.map(m, MediaFileResponseDto.class)).toList();
    }
}
