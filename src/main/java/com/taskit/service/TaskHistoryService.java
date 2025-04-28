package com.taskit.service;

import com.taskit.model.Task;
import com.taskit.model.TaskHistory;
import com.taskit.model.User;
import com.taskit.repository.TaskHistoryRepository;
import com.taskit.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    public List<TaskHistory> getTaskDetails(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByTimestampDesc(taskId);
    }

    public void saveHistory(Task task, User updatedBy, String action, String prev, String newVal) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setUpdatedBy(updatedBy);
        history.setAction(action);
        history.setPreviousValue(prev);
        history.setNewValue(newVal);
        history.setTimestamp(LocalDateTime.now());

        taskHistoryRepository.save(history);
    }

}
