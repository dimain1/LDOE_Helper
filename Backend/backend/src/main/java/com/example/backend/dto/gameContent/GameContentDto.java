package com.example.backend.dto.gameContent;

import java.util.Map;
import java.util.Set;

public class GameContentDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Map<String, Object> attributes;
    private Set<ContentTypeDto> types;
    private boolean pinned;

    public GameContentDto() {}

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }

    public String getName()                              { return name; }
    public void setName(String name)                     { this.name = name; }

    public String getDescription()                       { return description; }
    public void setDescription(String description)       { this.description = description; }

    public String getImageUrl()                          { return imageUrl; }
    public void setImageUrl(String imageUrl)             { this.imageUrl = imageUrl; }

    public Map<String, Object> getAttributes()                   { return attributes; }
    public void setAttributes(Map<String, Object> attributes)    { this.attributes = attributes; }

    public Set<ContentTypeDto> getTypes()                { return types; }
    public void setTypes(Set<ContentTypeDto> types)      { this.types = types; }

    public boolean isPinned()                            { return pinned; }
    public void setPinned(boolean pinned)                { this.pinned = pinned; }
}
