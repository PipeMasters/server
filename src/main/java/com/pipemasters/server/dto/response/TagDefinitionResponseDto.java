package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDefinitionResponseDto extends BaseDto {
    private String name;
    private String type;

    public TagDefinitionResponseDto() {
    }

    public TagDefinitionResponseDto(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}