package com.example.backend.dto.gameContent;

import java.util.HashMap;
import java.util.List;

public class GameContentRequest {
    private String name;
    private String description;
    private List<Long> typeIds;
    private HashMap<String, Object> attributes;

    public String getName()                              { return name; }
    public void setName(String name)                     { this.name = name; }

    public String getDescription()                       { return description; }
    public void setDescription(String description)       { this.description = description; }

    public List<Long> getTypeIds()                       { return typeIds; }
    public void setTypeIds(List<Long> typeIds)           { this.typeIds = typeIds; }

    public HashMap<String, Object> getAttributes()               { return attributes; }
    public void setAttributes(HashMap<String, Object> attributes){ this.attributes = attributes; }
}
