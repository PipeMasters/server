package com.pipemasters.server.dto.request;


import com.pipemasters.server.dto.BaseDto;
import jakarta.validation.constraints.NotNull;

public class BranchRequestDto extends BaseDto {
    @NotNull(message = "Name cannot be empty")
    private String name;
    private Long parentId;

    public BranchRequestDto() {
    }

    public BranchRequestDto(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}