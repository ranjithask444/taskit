package com.taskit.controller;

import com.taskit.model.Task;
import com.taskit.model.User;
import com.taskit.repository.UserRepository;
import com.taskit.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String viewDashboard(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Task> taskPage = dashboardService.getFilteredTasks(name, assignedTo, status, priority, pageable);

        List<User> allUsers = userRepository.findAll();

        model.addAttribute("taskPage", taskPage);
        model.addAttribute("users", allUsers);
        model.addAttribute("name", name);
        model.addAttribute("assignedTo", assignedTo);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("allStatuses", Task.Status.values());
        model.addAttribute("allPriorities", Task.Priority.values());

        return "dashboard";
    }




}
