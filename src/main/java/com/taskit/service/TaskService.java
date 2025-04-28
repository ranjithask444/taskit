package com.taskit.service;

import com.taskit.dto.TaskRequestDTO;
import com.taskit.model.Task;
import com.taskit.model.User;
import com.taskit.repository.TaskRepository;
import com.taskit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByAssignedUser(Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    public Task saveTask(Task existing) {
        return taskRepository.save(existing);
    }


    public Task createTask(TaskRequestDTO dto) {
        User assignedToUser = userRepository.findById(dto.getAssignedToId()).orElseThrow(() -> new IllegalArgumentException(" AssignedTo user not found."));
        User createdByUser = userRepository.findById(dto.getCreatedById()).orElseThrow(() -> new IllegalArgumentException("CreatedBy user not found."));

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStatus(dto.getStatus());
        task.setAssignedTo(assignedToUser);
        task.setCreatedBy(createdByUser);
        task.setDeadline(dto.getDeadline());

        return taskRepository.save(task);
    }

    public Task createTask(Task task) {
        task.setStatus(Task.Status.PENDING);
        if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
            User user = userRepository.findById(task.getAssignedTo().getId())
                    .orElse(null);
            task.setAssignedTo(user); // Attach a managed entity
        } else {
            task.setAssignedTo(null); // If nothing selected
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setCreatedBy(task.getAssignedTo());
        return taskRepository.save(task);
    }
}
