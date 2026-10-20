package com.pending.model;

import com.google.gson.annotations.SerializedName;
public class PendingItem {
    @SerializedName("id")
    private Long id;

    @SerializedName("ownerId")
    private Long ownerId;

    @SerializedName("title")
    private String title;

    @SerializedName("waitingOn")
    private String waitingOn;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("expectedResponseDate")
    private String expectedResponseDate;

    @SerializedName("status")
    private Integer status;

    @SerializedName("notes")
    private String notes;

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("resolvedAt")
    private String resolvedAt;

    @SerializedName("deletedAt")
    private String deletedAt;

    public PendingItem() {}

    public PendingItem(Long ownerId, String title, String waitingOn, String startDate,
                       String expectedResponseDate, Integer status, String notes, Integer priority) {
        this.ownerId = ownerId;
        this.title = title;
        this.waitingOn = waitingOn;
        this.startDate = startDate;
        this.expectedResponseDate = expectedResponseDate;
        this.status = status;
        this.notes = notes;
        this.priority = priority != null ? priority : 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getWaitingOn() { return waitingOn; }
    public void setWaitingOn(String waitingOn) { this.waitingOn = waitingOn; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getExpectedResponseDate() { return expectedResponseDate; }
    public void setExpectedResponseDate(String expectedResponseDate) { this.expectedResponseDate = expectedResponseDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
}