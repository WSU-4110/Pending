package com.pending.controller;

import com.pending.model.PendingItem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}