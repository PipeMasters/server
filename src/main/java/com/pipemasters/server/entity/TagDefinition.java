package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.TagType;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tag_definitions")
public class TagDefinition extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TagType type;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TagInstance> instances;

    protected TagDefinition() {
    }

    public TagDefinition(String name, TagType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    public List<TagInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<TagInstance> instances) {
        this.instances = instances;
    }
}