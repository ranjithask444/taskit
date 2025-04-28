package com.taskit.service;

import com.taskit.model.Task;
import com.taskit.model.User;
import com.taskit.repository.TaskRepository;
import com.taskit.repository.UserRepository;
import com.taskit.utils.Util;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;


    // Get the current logged-in user
    public User getCurrentUser() {
        String username = getLoggedInUsername();
        return userRepository.findByEmail(username).orElseThrow();
    }

    // Get the username of the currently logged-in user
    private String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // Get the dashboard data based on role
    public String getDashboardRole(User user) {
        if (user.getRole().toString().equals("ADMIN")) {
            return "admin-dashboard";
        } else if (user.getRole().toString().equals("MANAGER")) {
            return "manager-dashboard";
        } else {
            return "employee-dashboard";
        }
    }

    @Autowired
    private TaskRepository taskRepository;


    public Page<Task> getFilteredTasks(
            String name,
            Long userId,
            Task.Status status,
            Task.Priority priority,
            Pageable pageable) {

        System.out.println(name+" "+userId+ " " + status +" "+ priority );

        StringBuilder queryStr = new StringBuilder("SELECT t FROM Task t WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (name != null && !Util.isEmpty(name)) {
            queryStr.append("AND t.title = :title ");
            params.put("title", name);
        }
        if (userId != null) {
            queryStr.append("AND t.assignedTo.id = :userId ");
            params.put("userId", userId);
        }
        if (status != null) {
            queryStr.append("AND t.status = :status ");
            params.put("status", status);
        }
        if (priority != null) {
            queryStr.append("AND t.priority = :priority ");
            params.put("priority", priority);
        }

        // Create count query for total pages
        String countQueryStr = queryStr.toString().replaceFirst("SELECT t", "SELECT COUNT(t)");
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        // Create main query with pagination
        TypedQuery<Task> query = entityManager.createQuery(queryStr.toString(), Task.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Task> tasks = query.getResultList();

        return new PageImpl<>(tasks, pageable, total);
    }

    // You can add more business logic here, such as fetching tasks assigned to the user, etc.
}
