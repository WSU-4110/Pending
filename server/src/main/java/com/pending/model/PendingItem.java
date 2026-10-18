package com.pending.model;

import jakarta.persistence.*;


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

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public PendingItem() {
    }

    public PendingItem(Long ownerId,
                       String title,
                       String waitingOn,
                       LocalDate startDate,
                       LocalDate expectedResponseDate,
                       Integer status, String notes,
                       Integer priority,
                       OffsetDateTime createdAt,
                       OffsetDateTime updatedAt,
                       OffsetDateTime resolvedAt,
                       OffsetDateTime deletedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.waitingOn = waitingOn;
        this.startDate = startDate;
        this.expectedResponseDate = expectedResponseDate;
        this.status = status;
        this.notes = notes;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.deletedAt = deletedAt;
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

    public LocalDate getExpectedResponseDate() {
        return expectedResponseDate;
    }

    public void setExpectedResponseDate(LocalDate expectedResponseDate) {
        this.expectedResponseDate = expectedResponseDate;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}