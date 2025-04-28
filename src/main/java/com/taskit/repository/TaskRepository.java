package com.taskit.repository;

import com.taskit.model.Task;
import com.taskit.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User assignedTo);
    List<Task> findByCreatedBy(User createdBy);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByAssignedToIsNull();

    @Query("SELECT t FROM Task t " +
            "LEFT JOIN t.assignedTo a " +
            "WHERE (:name IS NULL OR LOWER(COALESCE(t.title, '')) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:assignedTo IS NULL OR a.id = :assignedTo) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findFilteredTasks(
            @Param("name") String name,
            @Param("assignedTo") Long assignedTo,
            @Param("status") Task.Status status,
            @Param("priority") Task.Priority priority,
            Pageable pageable
    );
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN t.assignedTo a " +
            "WHERE LOWER(COALESCE(t.title, '')) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND a.id = :assignedTo " +
            "AND  t.status = :status " +
            "AND t.priority = :priority")
    Page<Task> findFilteredTasks1(
            @Param("name") String name,
            @Param("assignedTo") Long assignedTo,
            @Param("status") Task.Status status,
            @Param("priority") Task.Priority priority,
            Pageable pageable
    );

    @Query("SELECT t FROM Task t ")
    Page<Task> findAllTasks(Pageable pageable);

}
