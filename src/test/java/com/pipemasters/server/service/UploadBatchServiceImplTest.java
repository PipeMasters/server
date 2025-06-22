package com.pipemasters.server.service;

import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.impl.UploadBatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadBatchServiceImplTest {

    @Mock
    private UploadBatchRepository uploadBatchRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UploadBatchServiceImpl uploadBatchService;

    @Test
    void getFilteredBatches_shouldReturnMappedPage() {
        UploadBatchFilter filter = new UploadBatchFilter();
        Pageable pageable = PageRequest.of(0, 10);

        UploadBatch batch = new UploadBatch();
        UploadBatchDto dto = new UploadBatchDto();

        Page<UploadBatch> entityPage = new PageImpl<>(List.of(batch));
        when(uploadBatchRepository.findFiltered(filter, pageable)).thenReturn(entityPage);
        when(modelMapper.map(batch, UploadBatchDto.class)).thenReturn(dto);

        Page<UploadBatchDto> result = uploadBatchService.getFilteredBatches(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
        verify(uploadBatchRepository).findFiltered(filter, pageable);
        verify(modelMapper).map(batch, UploadBatchDto.class);
    }
}