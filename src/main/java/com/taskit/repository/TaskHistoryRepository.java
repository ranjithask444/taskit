package com.taskit.repository;

import com.taskit.model.TaskHistory;
import com.taskit.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTask(Task task);
    List<TaskHistory> findByTaskIdOrderByTimestampDesc(Long taskId);

}
