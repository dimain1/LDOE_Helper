package com.example.backend.dto.template;

public class EventTemplateDto {
    private Long id;
    private Long creatorId;
    private String name;
    private String description;
    private String imageUrl;
    private Long duration;

    public EventTemplateDto() {}

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public Long getCreatorId()                  { return creatorId; }
    public void setCreatorId(Long creatorId)    { this.creatorId = creatorId; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public String getImageUrl()                 { return imageUrl; }
    public void setImageUrl(String imageUrl)    { this.imageUrl = imageUrl; }

    public Long getDuration()                   { return duration; }
    public void setDuration(Long duration)      { this.duration = duration; }
}
