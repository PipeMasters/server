package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.TagDefinitionResponseDto;
import com.pipemasters.server.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TagControllerTest {

    private final TagService tagService = mock(TagService.class);
    private final TagController tagController = new TagController(tagService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(tagController).build();

    @Test
    void getAllTags_ReturnsListOfTags() throws Exception {
        TagDefinitionResponseDto tag1 = new TagDefinitionResponseDto("Tag1", "RULE");
        TagDefinitionResponseDto tag2 = new TagDefinitionResponseDto("Tag2", "RULE");

        when(tagService.getAllTags()).thenReturn(List.of(tag1, tag2));

        mockMvc.perform(get("/api/v1/tag/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Tag1")))
                .andExpect(jsonPath("$[1].name", is("Tag2")));

        verify(tagService, times(1)).getAllTags();
    }

    @Test
    void getAllTags_ReturnsEmptyListWhenNoneExist() throws Exception {
        when(tagService.getAllTags()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tag/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(tagService, times(1)).getAllTags();
    }
}