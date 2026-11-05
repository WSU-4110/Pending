package com.pending.repository;

import com.pending.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    @Query("""
        SELECT r FROM Reminder r
        JOIN PendingItem t ON r.taskId = t.id
        WHERE r.taskId = :taskId
                AND t.ownerId = :ownerId
                AND t.deletedAt is NULL
        ORDER BY r.reminderTime ASC
    """)
    List<Reminder> findByTaskIdAndOwnerId(@Param("taskId") Long taskId, @Param("ownerId") Long ownerId);

    @Query("""
        SELECT r FROM Reminder r
        JOIN PendingItem t ON r.taskId = t.id
        WHERE t.ownerId = :ownerId
            AND t.deletedAt IS NULL
            AND t.resolvedAt IS NULL 
        ORDER BY r.reminderTime ASC
    """)
    List<Reminder> findAllActiveByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT r FROM Reminder r
        JOIN PendingItem t ON r.taskId = t.id
        WHERE r.id = :id AND t.ownerId = :ownerId
    """)
    Optional<Reminder> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    void deleteByReminderTimeBefore(OffsetDateTime time);

    @Modifying
    @Transactional
    void deleteByTaskId(Long taskId);
}
