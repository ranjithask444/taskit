package com.taskit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task_efficiency")
public class TaskEfficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @Column(nullable = false)
    private Float efficiencyScore; // AI-generated efficiency rating (0-100)

    @Column(name = "execution_time", nullable = false)
    private Integer executionTime; // Time taken in minutes
}
