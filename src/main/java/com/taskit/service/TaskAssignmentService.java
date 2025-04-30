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
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        return taskRepository.findByAssignedToIsNullAndStatusNot(Task.Status.COMPLETED);  // Assuming query method is defined to fetch unassigned tasks
    }

    private List<User> findMatchingEmployees(Task task) {
        log.info("Checking for skills in:= {}", task.getSkillsRequired());
        List<User> allUsers = userRepository.findBySkillsInAndAvailableBandwidthGreaterThan(task.getSkillsRequired(), task.getDeadline().getHour());
        log.info("Fetched all the users:= {}",allUsers.size());
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

    private String buildRequestBody(Task task, List<User> matchingUsers) {
        ObjectMapper mapper = new ObjectMapper();

        // Create the root object
        ObjectNode rootNode = mapper.createObjectNode();

        // Add task details
        rootNode.put("task_id", task.getId());
        rootNode.put("task_title", task.getTitle());
        rootNode.set("skills_required", formatSkills(task.getSkillsRequired()));  // Assuming this returns a JsonNode
        rootNode.put("priority", mapPriority(task.getPriority()));
        rootNode.put("deadline_hours", task.getDeadline().getHour());
        rootNode.put("available_bandwidth", 10); // Placeholder for available bandwidth

        // Prepare the candidate employees list
        ArrayNode userListJson = mapper.createArrayNode();
        int cnt = 0;
        for (User user : matchingUsers) {
            if (cnt > 10) break;
            cnt++;

            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("user_id", user.getId());
            userNode.set("skills", formatSkills(user.getSkills()));  // Assuming this returns a JsonNode
            userNode.put("available_bandwidth", user.getAvailableBandwidth());

            userListJson.add(userNode);
        }

        // Add candidate employees to the root object
        rootNode.set("candidate_employees", userListJson);

        // Return the JSON string representation of the root object
        return rootNode.toString();
    }

    private int mapPriority(Task.Priority priority) {
        return switch (priority) {
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private ArrayNode formatSkills(Set<Skill> skillsSet) {
        ObjectMapper mapper = new ObjectMapper();

        // Create an array node to represent the skills as a JSON array
        ArrayNode skillsArray = mapper.createArrayNode();

        // Iterate over the skills set and add each skill ID to the array
        for (Skill skill : skillsSet) {
            skillsArray.add(skill.getId());
        }

        // Return the string representation of the JSON array
        return skillsArray;
    }


    // Assign the employee to the task using the AI response
    private void assignEmployeeToTask(Task task, PredictionResponse prediction) {
        Optional<User> user = userRepository.findById(prediction.getSelectedEmployeeId());
        if (user.isPresent()) {
            task.setAssignedTo(user.get());
            task.setStatus(Task.Status.ACTIVE);  // Set the task status to ACTIVE once assigned
            taskRepository.save(task);
            User userObject = user.get();
            int availableBandwidth = userObject.getAvailableBandwidth() - task.getDeadline().getHour();
            userObject.setAvailableBandwidth(Math.max(availableBandwidth, 0));
            userRepository.save(userObject);
            log.info("Task {} assigned to User ID: {}", task.getTitle() , prediction.getSelectedEmployeeId());
        }
    }

    // Make the request to the AI API and assign the task based on the response
    public void assignTasks() {
        log.info("...................Called assignTasks.....................");
        List<Task> unassignedTasks = fetchUnassignedTasks();


        for (Task task : unassignedTasks) {
            log.info("Found Unassinged tasks: = {}", task);
            List<User> matchingUsers = findMatchingEmployees(task);


            if (!matchingUsers.isEmpty()) {
                log.info("Found Employees tasks: = {}",matchingUsers);
                try {
                    // Build the request body for AI prediction
                    String requestBody = buildRequestBody(task, matchingUsers);
                    log.info("Built request body : {}", requestBody);

                    // Make the API request
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/json");

                    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                    log.info("Making the http request....... {}", requestEntity);

                    ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                            aiServerUrl,
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
