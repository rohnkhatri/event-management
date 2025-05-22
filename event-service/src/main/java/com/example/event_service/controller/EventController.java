package com.example.event_service.controller;


import com.example.commonlib.dto.EventDto;

import com.example.event_service.service.EventService;
import com.example.event_service.entity.EventRegistration;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // For simple request body

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // --- Admin Operations ---

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Example: Security could be here or gateway
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventDto eventDto) {
        // In real app, verify user is ADMIN via JWT from gateway
        EventDto createdEvent = eventService.createEvent(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @Valid @RequestBody EventDto eventDto) {
        // In real app, verify user is ADMIN via JWT from gateway
        try {
            EventDto updatedEvent = eventService.updateEvent(id, eventDto);
            return ResponseEntity.ok(updatedEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or an error DTO
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        // In real app, verify user is ADMIN via JWT from gateway
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // --- User/Public Operations ---

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
        try {
            EventDto event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // --- User Registration Operations ---

    @PostMapping("/{eventId}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId,
                                              // In a real scenario, userId/username would come from the validated JWT (passed by gateway)
                                              @RequestHeader("X-User-Id") Long userId, // Assume gateway adds this header
                                              @RequestHeader("X-Username") String username) { // Assume gateway adds this header
        // NOTE: Passing user info via headers is one way, but can be insecure if not handled carefully.
        // A better way involves the gateway validating the JWT and ensuring the request is for the logged-in user.
        // For simplicity here, we use headers.
        try {
            EventRegistration registration = eventService.registerForEvent(eventId, userId, username);
            // Don't need to return the full registration object usually
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Successfully registered for event " + eventId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-registrations")
    public ResponseEntity<List<EventDto>> getMyRegisteredEvents(@RequestHeader("X-User-Id") Long userId) { // Assume gateway adds this header
        List<EventDto> events = eventService.getRegisteredEventsForUser(userId);
        return ResponseEntity.ok(events);
    }
}