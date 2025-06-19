package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.enums.Role;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto extends BaseDto{
    //    private String adSid;
    private String name;
    private String surname;
    private String patronymic;
    private Set<Role> roles;
    private BranchDto branch;

    public UserDto() {
    }

    public UserDto( String name, String surname, String patronymic, Set<Role> roles, BranchDto branch) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.roles = roles;
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public BranchDto getBranch() {
        return branch;
    }

    public void setBranch(BranchDto branch) {
        this.branch = branch;
    }
}
