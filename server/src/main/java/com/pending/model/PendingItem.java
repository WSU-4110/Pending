package com.pending.model;

public class PendingItem {

    private Long id;
    private String title;
    private String waitingOn;
    private String description;
    private String status;

    public PendingItem() {
    }

    public PendingItem(Long id, String title, String waitingOn,
                       String description, String status) {
        this.id = id;
        this.title = title;
        this.waitingOn = waitingOn;
        this.description = description;
        this.status = status;
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
}