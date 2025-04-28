package com.taskit.controller;

import com.taskit.model.Task;
import com.taskit.model.TaskEfficiency;
import com.taskit.model.TaskHistory;
import com.taskit.model.User;
import com.taskit.repository.UserRepository;
import com.taskit.service.TaskAssignmentService;
import com.taskit.service.TaskEfficiencyService;
import com.taskit.service.TaskHistoryService;
import com.taskit.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/ai/tasks/")
public class TaskAssignController {

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @PostMapping("/assign")
    public String createTask(Model model) {
        taskAssignmentService.assignTasks();
        return "redirect:/dashboard";
    }

}
