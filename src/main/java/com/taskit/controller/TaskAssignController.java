package com.taskit.controller;

import com.taskit.service.TaskAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
