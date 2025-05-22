package com.example.event_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations", uniqueConstraints = {
        // Ensure a user can register for an event only once
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
@Data
@NoArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store the User ID from the user-service. We don't have a direct foreign key
    // relationship across microservices databases in this simple setup.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Store username for easier retrieval/display if needed, avoids calling user-service every time
    @Column(name = "username", nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDateTime registrationTime;

    public EventRegistration(Long userId, String username, Event event) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.registrationTime = LocalDateTime.now();
    }
}