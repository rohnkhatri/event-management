package com.example.event_service.repository;

import com.example.event_service.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByUserIdAndEventId(Long userId, Long eventId);
    List<EventRegistration> findByUserId(Long userId);
    List<EventRegistration> findByEventId(Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}