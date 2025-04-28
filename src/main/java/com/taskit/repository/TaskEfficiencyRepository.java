package com.taskit.repository;

import com.taskit.model.TaskEfficiency;
import com.taskit.model.Task;
import com.taskit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskEfficiencyRepository extends JpaRepository<TaskEfficiency, Long> {
    List<TaskEfficiency> findByAssignedTo(User assignedTo);
    List<TaskEfficiency> findByTask(Task task);
    List<TaskEfficiency> findByTaskId(Long taskId);
}
