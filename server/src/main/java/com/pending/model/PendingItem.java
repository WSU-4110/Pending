package com.pending.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tasks")
public class PendingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(name = "waiting_on")
    private String waitingOn;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_response_date")
    private LocalDate expectedResponseDate;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public PendingItem() {
    }

    public PendingItem(
            Long id,
            String title,
            String waitingOn,
            String description,
            String status,
            LocalDate initiatedDate,
            LocalDate expectedResponseDate,
            LocalDate reminderDate) {

        this.id = id;
        this.title = title;
        this.waitingOn = waitingOn;
        this.description = description;
        this.status = status;
        this.initiatedDate = initiatedDate;
        this.expectedResponseDate = expectedResponseDate;
        this.reminderDate = reminderDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWaitingOn() {
        return waitingOn;
    }

    public void setWaitingOn(String waitingOn) {
        this.waitingOn = waitingOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getInitiatedDate() {
        return initiatedDate;
    }

    public void setInitiatedDate(LocalDate initiatedDate) {
        this.initiatedDate = initiatedDate;
    }

    public LocalDate getExpectedResponseDate() {
        return expectedResponseDate;
    }

    public void setExpectedResponseDate(LocalDate expectedResponseDate) {
        this.expectedResponseDate = expectedResponseDate;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }
}