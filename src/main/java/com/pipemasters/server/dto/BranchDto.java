package com.pipemasters.server.dto;


public class BranchDto extends BaseDto{
    private String name;
    private BranchDto parent;

    public BranchDto() {
    }

    public BranchDto(Long id, String name, BranchDto parent) {
        super(id);
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
