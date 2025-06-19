package com.pipemasters.server.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BranchDto extends BaseDto{
    @NotNull(message = "Name cannot be empty")
    private String name;
    private BranchDto parent;

    public BranchDto() {
    }

    public BranchDto(String name, BranchDto parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BranchDto getParent() {
        return parent;
    }

    public void setParent(BranchDto parent) {
        this.parent = parent;
    }
}
