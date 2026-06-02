package com.example.backend.dto.event;

import java.time.Instant;

public class EventCreateRequest {
    private Long templateId;    // nullable — событие можно создать без шаблона
    private String name;
    private String description;
    private String imageUrl;
    private Instant startTime;
    private Instant endTime;

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
