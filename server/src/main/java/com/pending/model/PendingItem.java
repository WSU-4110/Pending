package com.pending.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class PendingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Waiting on is required")
    private String waitingOn;

    private String description;

    @NotBlank(message = "Status is required")
    private String status;

    private LocalDate initiatedDate;
    private LocalDate expectedResponseDate;
    private LocalDate reminderDate;

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