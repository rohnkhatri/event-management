package com.example.event_service.service;



import com.example.commonlib.dto.EventDto;

import com.example.event_service.entity.Event;
import com.example.event_service.entity.EventRegistration;
import com.example.event_service.repository.EventRegistrationRepository;
import com.example.event_service.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    // --- Event CRUD (Admin) ---

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToEventDto)
                .collect(Collectors.toList());
    }

    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToEventDto(event);
    }

    public EventDto createEvent(EventDto eventDto) {
        Event event = mapToEventEntity(eventDto);
        Event savedEvent = eventRepository.save(event);
        return mapToEventDto(savedEvent);
    }

    public EventDto updateEvent(Long id, EventDto eventDto) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        existingEvent.setName(eventDto.getName());
        existingEvent.setDescription(eventDto.getDescription());
        existingEvent.setStartTime(eventDto.getStartTime());
        existingEvent.setEndTime(eventDto.getEndTime());
        existingEvent.setLocation(eventDto.getLocation());

        Event updatedEvent = eventRepository.save(existingEvent);
        return mapToEventDto(updatedEvent);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        // Registrations are handled by cascade delete due to relationship mapping
        eventRepository.deleteById(id);
    }

    // --- User Event Registration ---

    @Transactional
    public EventRegistration registerForEvent(Long eventId, Long userId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check if already registered
        if (registrationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new IllegalStateException("User already registered for this event");
        }

        // In a real app, you might want to check if the user ID actually exists
        // by calling the user-service, but we'll keep it simple here.

        EventRegistration registration = new EventRegistration(userId, username, event);
        return registrationRepository.save(registration);
    }

    public List<EventDto> getRegisteredEventsForUser(Long userId) {
        List<EventRegistration> registrations = registrationRepository.findByUserId(userId);
        return registrations.stream()
                .map(reg -> mapToEventDto(reg.getEvent()))
                .collect(Collectors.toList());
    }

    // --- Mappers ---
    private EventDto mapToEventDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation()
        );
    }

    private Event mapToEventEntity(EventDto dto) {
        Event event = new Event();
        // ID is usually null for creation, set for updates if needed (handled by update logic)
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setLocation(dto.getLocation());
        return event;
    }
}