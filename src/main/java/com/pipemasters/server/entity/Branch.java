package com.pipemasters.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "branches",
        indexes = {@Index(columnList = "parent_id")})
public class Branch extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Branch parent;         // для иерархии филиалов

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
}