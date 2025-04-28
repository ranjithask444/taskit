package com.taskit.dto;

import com.taskit.model.Task.Priority;
import com.taskit.model.Task.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequestDTO {
    private String title;
    private String description;
    private Priority priority;
    private Status status = Status.PENDING; // Optional, defaults to PENDING
    private Long assignedToId;
    private Long createdById;
    private LocalDateTime deadline;
}
