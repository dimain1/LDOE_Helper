package com.example.backend.mediator;

import com.example.backend.dto.event.EventCreateRequest;
import com.example.backend.dto.event.EventDto;
import com.example.backend.dto.event.EventUpdateRequest;
import com.example.backend.entity.Event;
import com.example.backend.entity.EventTemplate;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.foundation.repository.EventRepository;
import com.example.backend.foundation.repository.EventTemplateRepository;
import com.example.backend.foundation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTemplateRepository templateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        UserRole role = new UserRole();
        role.setName("USER");

        user = new User("alice", "hashed", "alice@mail.com", role);
        user.setId(1L);

        event = new Event();
        event.setId(10L);
        event.setUser(user);
        event.setName("Raid cooldown");
        event.setStartTime(Instant.now());
        event.setEndTime(Instant.now().plusSeconds(3600));
    }

    // ── getMyEvents ────────────────────────────────────────────────────────────

    @Test
    void getMyEvents_returnsUserEvents() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(eventRepository.findByUser(user)).thenReturn(List.of(event));

        List<EventDto> result = eventService.getMyEvents("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Raid cooldown");
    }

    @Test
    void getMyEvents_noEvents_returnsEmptyList() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(eventRepository.findByUser(user)).thenReturn(List.of());

        assertThat(eventService.getMyEvents("alice")).isEmpty();
    }

    @Test
    void getMyEvents_unknownLogin_throws() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getMyEvents("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_withoutTemplate_savesEventAndReturnsDto() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(eventRepository.save(any())).thenReturn(event);

        EventCreateRequest req = new EventCreateRequest();
        req.setName("Raid cooldown");
        req.setStartTime(Instant.now());
        req.setEndTime(Instant.now().plusSeconds(3600));

        EventDto dto = eventService.create("alice", req);

        assertThat(dto.getName()).isEqualTo("Raid cooldown");
        verify(eventRepository).save(any(Event.class));
        verify(templateRepository, never()).findById(any());
    }

    @Test
    void create_withTemplate_loadsAndSetsTemplate() {
        EventTemplate template = new EventTemplate();
        template.setId(5L);
        template.setName("Base template");
        template.setDuration(3600L);

        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(template));
        when(eventRepository.save(any())).thenReturn(event);

        EventCreateRequest req = new EventCreateRequest();
        req.setTemplateId(5L);
        req.setName("Raid cooldown");
        req.setStartTime(Instant.now());
        req.setEndTime(Instant.now().plusSeconds(3600));

        eventService.create("alice", req);

        verify(templateRepository).findById(5L);
    }

    @Test
    void create_templateNotFound_throws() {
        when(userRepository.findByLogin("alice")).thenReturn(Optional.of(user));
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        EventCreateRequest req = new EventCreateRequest();
        req.setTemplateId(99L);
        req.setName("Event");
        req.setStartTime(Instant.now());
        req.setEndTime(Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> eventService.create("alice", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Template not found");
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    void update_ownedEvent_updatesNameAndDescription() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);

        EventUpdateRequest req = new EventUpdateRequest();
        req.setName("Updated name");
        req.setDescription("new desc");

        eventService.update("alice", 10L, req);

        assertThat(event.getName()).isEqualTo("Updated name");
        assertThat(event.getDescription()).isEqualTo("new desc");
    }

    @Test
    void update_nullFields_doesNotOverwriteExistingValues() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);

        EventUpdateRequest req = new EventUpdateRequest();
        // all fields null — no changes expected

        eventService.update("alice", 10L, req);

        assertThat(event.getName()).isEqualTo("Raid cooldown");
    }

    @Test
    void update_eventNotFound_throws() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.update("alice", 999L, new EventUpdateRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void update_callerIsNotOwner_throwsAccessDenied() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.update("bob", 10L, new EventUpdateRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ownedEvent_callsRepositoryDelete() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        eventService.delete("alice", 10L);

        verify(eventRepository).delete(event);
    }

    @Test
    void delete_callerIsNotOwner_throwsAndDoesNotDelete() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.delete("bob", 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        verify(eventRepository, never()).delete(any());
    }

    @Test
    void delete_eventNotFound_throws() {
        when(eventRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.delete("alice", 404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event not found");
    }
}
