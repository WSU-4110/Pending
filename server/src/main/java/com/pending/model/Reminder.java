package com.pending.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Audited;

import java.time.OffsetDateTime;

@Entity
@Audited.Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Task ID is required")
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @NotNull(message = "Reminder time is required")
    @Column(name = "reminder_time", nullable = false)
    private OffsetDateTime reminderTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Reminder() {}

    public Reminder(Long taskId, OffsetDateTime reminderTime, OffsetDateTime createdAt) {
        this.taskId = taskId;
        this.reminderTime = reminderTime;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public OffsetDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(OffsetDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
