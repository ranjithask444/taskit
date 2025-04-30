package com.taskit.service;

import com.taskit.dto.PredictionResponse;
import com.taskit.model.Skill;
import com.taskit.model.Task;
import com.taskit.model.User;
import com.taskit.repository.TaskRepository;
import com.taskit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TaskAssignmentService {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private List<Task> fetchUnassignedTasks() {
        return taskRepository.findByAssignedToIsNull();  // Assuming query method is defined to fetch unassigned tasks
    }

    private List<User> findMatchingEmployees(Task task) {
        System.out.println("Checking for skills in:= "+ task.getSkillsRequired());
        List<User> allUsers = userRepository.findBySkillsIn(task.getSkillsRequired());
        System.out.println("Fetched all the users:= "+allUsers.size());
        return allUsers;
    }

    // Check if any skills match between task and user
    private boolean skillMatch(HashSet<String> taskSkills, List<String> userSkills) {
        for (String skill : userSkills) {
            if (taskSkills.contains(skill)) {
                return true;
            }
        }
        return false;
    }

    // Build the API request body for the AI server
    private String buildRequestBody(Task task, List<User> matchingUsers) {
        // Prepare the JSON payload for the AI API call
        StringBuilder userListJson = new StringBuilder("[");
        int cnt = 0;
        for (User user : matchingUsers) {
            if(cnt>10)
                break;
            cnt++;
            userListJson.append("{")
                    .append("\"user_id\": ").append(user.getId()).append(",")
                    .append("\"skills\": ").append(formatSkills(user.getSkills())).append(",")
                    .append("\"available_bandwidth\": ").append(user.getAvailableBandwidth())
                    .append("},");
        }
        // Remove the last comma if matchingUsers is not empty
        if (matchingUsers.size() > 0) {
            userListJson.setLength(userListJson.length() - 1);
        }
        userListJson.append("]");

        int priority = 0;
        if(task.getPriority() == Task.Priority.HIGH){
            priority = 1;
        }
        if(task.getPriority() == Task.Priority.MEDIUM){
            priority = 2;
        }
        if(task.getPriority() == Task.Priority.LOW){
            priority = 3;
        }

        return "{"
                + "\"task_id\": " + task.getId() + ","
                + "\"task_title\": \"" + task.getTitle() + "\","
                + "\"skills_required\": " + formatSkills(task.getSkillsRequired()) + ","
                + "\"priority\": \"" + priority + "\","
                + "\"deadline_hours\": " + task.getDeadline().getHour() + ","
                + "\"candidate_employees\": " + userListJson.toString()
                + "}";
    }

    private String formatSkills(Set<Skill> skillsSet) {
        StringBuilder skillsJson = new StringBuilder("[");
        List<Skill> skills = skillsSet.stream().toList();
        for (Skill skill : skills) {
            skillsJson.append("\"").append(skill.getId()).append("\",");
        }
        if (skillsJson.length() > 1) {
            skillsJson.setLength(skillsJson.length() - 1); // Remove trailing comma
        }
        skillsJson.append("]");
        return skillsJson.toString();
    }


    // Assign the employee to the task using the AI response
    private void assignEmployeeToTask(Task task, PredictionResponse prediction) {
        Optional<User> user = userRepository.findById(prediction.getSelectedEmployeeId());
        if (user.isPresent()) {
            task.setAssignedTo(user.get());
            task.setStatus(Task.Status.ACTIVE);  // Set the task status to ACTIVE once assigned
            taskRepository.save(task);
            System.out.println("Task " + task.getTitle() + " assigned to User ID: " + prediction.getSelectedEmployeeId());
        }
    }

    // Make the request to the AI API and assign the task based on the response
    public void assignTasks() {
        System.out.println("...................Called assignTasks.....................");
        List<Task> unassignedTasks = fetchUnassignedTasks();


        for (Task task : unassignedTasks) {
            System.out.println("Found Unassinged tasks: = "+task);
            List<User> matchingUsers = findMatchingEmployees(task);


            if (!matchingUsers.isEmpty()) {
                System.out.println("Found Employees tasks: = "+matchingUsers);
                try {
                    // Build the request body for AI prediction
                    String requestBody = buildRequestBody(task, matchingUsers);
                    log.info("Built request body : {}", requestBody);

                    // Make the API request
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/json");

                    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                    log.info("Making the http request.......");

                    ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                            aiServerUrl + "/predict-assignment",
                            HttpMethod.POST,
                            requestEntity,
                            PredictionResponse.class
                    );

                    // If the AI response is successful, assign the employee to the task
                    if (response.getStatusCode().is2xxSuccessful()) {
                        PredictionResponse prediction = response.getBody();
                        log.info("Response received:= {}",prediction);
                        assignEmployeeToTask(task, prediction);
                    } else {
                        log.error("Failed to assign task : {}", task.getTitle());
                    }
                } catch (Exception e) {
                    log.error("Error while calling AI server: ", e);
                }
            }
        }
    }
}
