package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.TagDefinitionResponseDto;
import com.pipemasters.server.entity.TagDefinition;
import com.pipemasters.server.entity.enums.TagType;
import com.pipemasters.server.repository.TagDefinitionRepository;
import com.pipemasters.server.repository.TagInstanceRepository;
import com.pipemasters.server.service.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TagServiceImplTest {

    private TagDefinitionRepository tagDefinitionRepository;
    private TagInstanceRepository tagInstanceRepository;
    private TranscriptFragmentService transcriptFragmentService;
    private ModelMapper modelMapper;
    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagDefinitionRepository = mock(TagDefinitionRepository.class);
        tagInstanceRepository = mock(TagInstanceRepository.class);
        transcriptFragmentService = mock(TranscriptFragmentService.class);
        modelMapper = new ModelMapper();

        tagService = new TagServiceImpl(
                tagDefinitionRepository,
                tagInstanceRepository,
                transcriptFragmentService,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                modelMapper,
                "http://fake-url",
                "token"
        );
    }

    @Test
    void getAllTags_ShouldReturnMappedTagDefinitionDtos() {
        TagDefinition def1 = new TagDefinition("Tag1", TagType.RULE);
        TagDefinition def2 = new TagDefinition("Tag2", TagType.RULE);

        when(tagDefinitionRepository.findAll()).thenReturn(List.of(def1, def2));

        List<TagDefinitionResponseDto> result = tagService.getAllTags();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Tag1");
        assertThat(result.get(1).getName()).isEqualTo("Tag2");
        verify(tagDefinitionRepository, times(1)).findAll();
    }
}
