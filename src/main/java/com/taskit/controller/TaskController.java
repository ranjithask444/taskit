package com.taskit.controller;

import com.taskit.model.Task;
import com.taskit.model.TaskEfficiency;
import com.taskit.model.TaskHistory;
import com.taskit.model.User;
import com.taskit.repository.UserRepository;
import com.taskit.service.SkillService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Autowired
    private TaskEfficiencyService taskEfficiencyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillService skillService;

    @GetMapping("/user/{userId}/tasks")
    public String viewUserTasks(@PathVariable Long userId, Model model) {
        List<Task> tasks = taskService.getTasksByAssignedUser(userId);
        model.addAttribute("tasks", tasks);
        return "user_tasks";
    }

    @GetMapping("/{taskId}")
    public String getViewTaskDetails(@PathVariable Long taskId, Model model) {
        Task task = taskService.getTaskById(taskId);
        List<TaskHistory> historyList = taskHistoryService.getTaskDetails(taskId);
        List<TaskEfficiency> efficiencies = taskEfficiencyService.getTaskEfficiencyByTaskId(taskId);
        model.addAttribute("task", task);
        model.addAttribute("historyList", historyList);
        return "task_details";
    }

    @GetMapping("/edit/2")
    public String showEditForm( Model model) {
        Task task = taskService.getTaskById(2L);
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("task", task);
        model.addAttribute("users", allUsers);
        model.addAttribute("priorities", Task.Priority.values());
        return "task_edit";
    }

    @PostMapping("/update")
    public String updateTask(@ModelAttribute("task") Task updatedTask) {
        Task existing = taskService.getTaskById(updatedTask.getId());
        if (existing == null) return "error/404";
        // Example of getting logged-in user (mocked for now)
        User updatedBy = userRepository.findById(1L).orElse(null); // Replace with actual logged-in user
        // Track changes
        if (!existing.getTitle().equals(updatedTask.getTitle())) {
            taskHistoryService.saveHistory(existing, updatedBy, "Title Changed", existing.getTitle(), updatedTask.getTitle());
            existing.setTitle(updatedTask.getTitle());
        }
        if (existing.getPriority() != updatedTask.getPriority()) {
            taskHistoryService.saveHistory(existing, updatedBy, "Priority Changed", existing.getPriority().name(), updatedTask.getPriority().name());
            existing.setPriority(updatedTask.getPriority());
        }
        if (existing.getDeadline() != null && !existing.getDeadline().equals(updatedTask.getDeadline())
                || (existing.getDeadline() == null && updatedTask.getDeadline() != null)) {
            taskHistoryService.saveHistory(existing, updatedBy, "Deadline Changed",
                    existing.getDeadline() == null ? "None" : existing.getDeadline().toString(),
                    updatedTask.getDeadline() == null ? "None" : updatedTask.getDeadline().toString());
            existing.setDeadline(updatedTask.getDeadline());
        }

        if (existing.getAssignedTo() != null && !existing.getAssignedTo().getId().equals(updatedTask.getAssignedTo().getId())) {
            taskHistoryService.saveHistory(existing, updatedBy, "Assignee Changed",
                    existing.getAssignedTo().getName(), updatedTask.getAssignedTo().getName());
            existing.setAssignedTo(updatedTask.getAssignedTo());
        }

        taskService.saveTask(existing);
        return "redirect:/tasks/" + updatedTask.getId();
    }

    @GetMapping("/add")
    public String showCreateTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("priorities", Task.Priority.values());
        model.addAttribute("skills", skillService.getAvailableSkills());
        return "task-form";
    }

    @PostMapping("/add")
    public String createTask(@ModelAttribute Task task, @RequestParam("skillIds") List<Long> skills) {
        task.setSkillsRequired(new HashSet<>(skillService.getSkillsById(skills)));
        taskService.createTask(task);
        return "redirect:/dashboard";
    }

}
