package com.pipemasters.server.dto.request.update;

import com.pipemasters.server.entity.enums.Role;

import java.util.Set;

public class UserUpdateDto {
    // private String adSid;

    private String name;
    private String surname;
    private String patronymic;
    private Set<Role> roles;
    private Long branchId;

    public UserUpdateDto() {
    }

    public UserUpdateDto(String name, String surname, String patronymic, Set<Role> roles, Long branchId) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.roles = roles;
        this.branchId = branchId;
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

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
}
