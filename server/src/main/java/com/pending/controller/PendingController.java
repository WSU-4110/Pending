package com.pending.controller;

import com.pending.model.PendingItem;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PendingController {

    private final List<PendingItem> items = new ArrayList<>();
    private long nextId = 1;

    @GetMapping("/test")
    public String testServer() {
        return "Pending server is running!";
    }

    @GetMapping("/items")
    public List<PendingItem> getAllItems() {
        return items;
    }

    @PostMapping("/items")
    public PendingItem createItem(@RequestBody PendingItem item) {
        item.setId(nextId++);
        items.add(item);
        return item;
    }

    @PutMapping("/items/{id}")
    public PendingItem updateItem(
            @PathVariable Long id,
            @RequestBody PendingItem updatedItem) {

        for (PendingItem item : items) {
            if (item.getId().equals(id)) {
                item.setTitle(updatedItem.getTitle());
                item.setWaitingOn(updatedItem.getWaitingOn());
                item.setDescription(updatedItem.getDescription());
                item.setStatus(updatedItem.getStatus());

                return item;
            }
        }

        return null;
    }

    @DeleteMapping("/items/{id}")
    public String deleteItem(@PathVariable Long id) {

        boolean removed = items.removeIf(item -> item.getId().equals(id));

        if (removed) {
            return "Pending item deleted.";
        }

        return "Pending item not found.";
    }
}