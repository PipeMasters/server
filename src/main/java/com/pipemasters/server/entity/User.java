package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.Role;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users"
//        , uniqueConstraints = @UniqueConstraint(columnNames = "ad_sid")
)
public class User extends BaseEntity {

//    на будущее, для астры и тд.
//    @Column(name = "ad_sid", nullable = false, length = 64)
//    private String adSid;

    private String name;
    private String surname;
    private String patronymic;

    //    много ролей — одна таблица связей
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    //    принадлежность к филиалу
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public User(String name, String surname, String patronymic, Set<Role> roles, Branch branch) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.roles = roles;
        this.branch = branch;
    }

    public User() {
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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
    public String getFullName() {
        return String.join(" ", surname, name, patronymic);
    }
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", roles=" + roles +
                ", branch=" + branch +
                '}';
    }
}
