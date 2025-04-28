package com.taskit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
                            
    @ManyToOne
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy; // Who updated the task

    @Column(nullable = false)
    private String action; // Example: "Task Updated", "Status Changed", "Reassigned"

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue; // Stores previous state of the task

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // Stores updated state

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
