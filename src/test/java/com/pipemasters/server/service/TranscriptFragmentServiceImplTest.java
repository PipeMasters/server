package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.TranscriptFragment;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.TranscriptFragmentRepository;
import com.pipemasters.server.service.impl.TranscriptFragmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptFragmentServiceImplTest {

    @Mock
    private TranscriptFragmentRepository repository;
    @Mock
    private MediaFileRepository mediaFileRepository;
    @Mock
    private ModelMapper modelMapper;

    private TranscriptFragmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TranscriptFragmentServiceImpl(repository, mediaFileRepository, "token", modelMapper);
    }

    @Test
    void search_MapsResults() {
        TranscriptFragment fragment = new TranscriptFragment();
        SttFragmentDto dto = new SttFragmentDto();
        when(repository.search("q")).thenReturn(List.of(fragment));
        when(modelMapper.map(fragment, SttFragmentDto.class)).thenReturn(dto);

        List<SttFragmentDto> result = service.search("q");

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getByMediaFile_NotFound() {
        when(mediaFileRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(MediaFileNotFoundException.class, () -> service.getByMediaFile(1L));
    }

    @Test
    void getByMediaFile_MapsFragments() {
        TranscriptFragment fragment = new TranscriptFragment();
        MediaFile mediaFile = new MediaFile();
        mediaFile.setTranscriptFragments(List.of(fragment));
        when(mediaFileRepository.findById(1L)).thenReturn(Optional.of(mediaFile));
        SttFragmentDto dto = new SttFragmentDto();
        when(modelMapper.map(fragment, SttFragmentDto.class)).thenReturn(dto);

        List<SttFragmentDto> result = service.getByMediaFile(1L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }
}