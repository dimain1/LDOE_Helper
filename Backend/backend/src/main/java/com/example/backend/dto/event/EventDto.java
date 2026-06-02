package com.example.backend.dto.event;

import java.time.Instant;

public class EventDto {
    private Long id;
    private Long userId;
    private Long templateId;
    private String name;
    private String description;
    private String imageUrl;
    private Instant startTime;
    private Instant endTime;

    public EventDto() {}

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public Long getUserId()                     { return userId; }
    public void setUserId(Long userId)          { this.userId = userId; }

    public Long getTemplateId()                 { return templateId; }
    public void setTemplateId(Long templateId)  { this.templateId = templateId; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public String getImageUrl()                 { return imageUrl; }
    public void setImageUrl(String imageUrl)    { this.imageUrl = imageUrl; }

    public Instant getStartTime()               { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime()                 { return endTime; }
    public void setEndTime(Instant endTime)     { this.endTime = endTime; }
}
