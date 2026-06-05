package com.example.backend.dto.template;

public class EventTemplateUpdateRequest {
    private String name;
    private String description;
    private Long duration;

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public String getDescription()          { return description; }
    public void setDescription(String desc) { this.description = desc; }

    public Long getDuration()               { return duration; }
    public void setDuration(Long duration)  { this.duration = duration; }
}
