package com.example.backend.control;

import com.example.backend.dto.event.EventCreateRequest;
import com.example.backend.dto.event.EventDto;
import com.example.backend.dto.event.EventUpdateRequest;
import com.example.backend.mediator.EventService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** Все события текущего пользователя. */
    @GetMapping
    public List<EventDto> getMyEvents(@AuthenticationPrincipal UserDetails userDetails) {
        return eventService.getMyEvents(userDetails.getUsername());
    }

    /** Создать новое событие. */
    @PostMapping
    public EventDto create(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestBody EventCreateRequest request) {
        return eventService.create(userDetails.getUsername(), request);
    }

    /** Обновить существующее событие (только своё). */
    @PutMapping("/{id}")
    public EventDto update(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id,
                           @RequestBody EventUpdateRequest request) {
        return eventService.update(userDetails.getUsername(), id, request);
    }

    /** Удалить событие (только своё). */
    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id) {
        eventService.delete(userDetails.getUsername(), id);
    }
}
