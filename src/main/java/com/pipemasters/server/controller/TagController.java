package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.TagDefinitionResponseDto;
import com.pipemasters.server.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tag")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/all")
    public List<TagDefinitionResponseDto> getAllTags() {
        return tagService.getAllTags();
    }
}
