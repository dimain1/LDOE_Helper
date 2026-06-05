package com.example.backend.mediator;

import com.example.backend.dto.event.EventCreateRequest;
import com.example.backend.dto.event.EventDto;
import com.example.backend.dto.event.EventUpdateRequest;
import com.example.backend.entity.Event;
import com.example.backend.entity.EventTemplate;
import com.example.backend.entity.User;
import com.example.backend.foundation.repository.EventRepository;
import com.example.backend.foundation.repository.EventTemplateRepository;
import com.example.backend.foundation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventTemplateRepository templateRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository,
                        EventTemplateRepository templateRepository,
                        UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
    }

    /** Все события текущего пользователя. */
    public List<EventDto> getMyEvents(String login) {
        User user = findUser(login);
        return eventRepository.findByUser(user)
                .stream()
                .map(EventService::toDto)
                .toList();
    }

    @Transactional
    public EventDto create(String login, EventCreateRequest request) {
        User user = findUser(login);

        EventTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new RuntimeException("Template not found: " + request.getTemplateId()));
        }

        Event event = new Event();
        event.setUser(user);
        event.setTemplate(template);
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setImageUrl(request.getImageUrl());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        return toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto update(String login, Long eventId, EventUpdateRequest request) {
        Event event = findOwnedEvent(login, eventId);

        if (request.getName()        != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getImageUrl()    != null) event.setImageUrl(request.getImageUrl());
        if (request.getStartTime()   != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime()     != null) event.setEndTime(request.getEndTime());

        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void delete(String login, Long eventId) {
        Event event = findOwnedEvent(login, eventId);
        eventRepository.delete(event);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Event findOwnedEvent(String login, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        if (!event.getUser().getLogin().equals(login)) {
            throw new RuntimeException("Access denied");
        }
        return event;
    }

    public static EventDto toDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setUserId(event.getUser().getId());
        dto.setTemplateId(event.getTemplate() != null ? event.getTemplate().getId() : null);
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setImageUrl(event.getImageUrl());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        return dto;
    }
}
