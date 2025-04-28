package com.taskit.service;

import com.taskit.model.TaskEfficiency;
import com.taskit.repository.TaskEfficiencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskEfficiencyService {

    @Autowired
    private TaskEfficiencyRepository taskEfficiencyRepository;

    public List<TaskEfficiency> getTaskEfficiencyByTaskId(Long taskId){
        return taskEfficiencyRepository.findByTaskId(taskId);
    }
}
