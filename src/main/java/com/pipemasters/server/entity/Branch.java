package com.pipemasters.server.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches",
        indexes = {@Index(columnList = "parent_id")})
public class Branch extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Branch parent;         // для иерархии филиалов

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    public Branch(String name, Branch parent) {
        this.name = name;
        this.parent = parent;
    }

    protected Branch() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Branch getParent() {
        return parent;
    }

    public void setParent(Branch parent) {
        this.parent = parent;
    }

    public Integer getBranchLevel() {
        if (parent == null) {
            return 0;
        }
        return parent.getBranchLevel() + 1;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                ", users=" + users +
                '}';
    }
}