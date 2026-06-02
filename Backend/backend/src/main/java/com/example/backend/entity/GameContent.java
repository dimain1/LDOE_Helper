package com.example.backend.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_content")
public class GameContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "image")
    private String imageUrl;

    // Переименовано с properties → attributes (согласовано с Room-схемой)
    @Column(name = "attributes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes = new HashMap<>();

    @ManyToMany
    @JoinTable(
        name = "type_to_content",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private Set<ContentType> types = new HashSet<>();

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public String getImageUrl()                      { return imageUrl; }
    public void setImageUrl(String imageUrl)         { this.imageUrl = imageUrl; }

    public Map<String, Object> getAttributes()               { return attributes; }
    public void setAttributes(Map<String, Object> attributes){ this.attributes = attributes; }

    public Set<ContentType> getTypes()               { return types; }
    public void setTypes(Set<ContentType> types)     { this.types = types; }
}
