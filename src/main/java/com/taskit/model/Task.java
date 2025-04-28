package com.taskit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "deadline")
    private LocalDateTime deadline;

    // 🔥 NEW: Skills required for the task
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_skills", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "skill_required")
    private List<String> skillsRequired;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum Status {
        ACTIVE, COMPLETED, PENDING
    }
}
