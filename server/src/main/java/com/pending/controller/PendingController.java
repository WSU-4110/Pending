package com.pending.controller;

import com.pending.model.PendingItem;
import com.pending.repository.PendingItemRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PendingController {

    private final PendingItemRepository repository;

    public PendingController(PendingItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/test")
    public String testServer() {
        return "Pending server is running!";
    }

    @GetMapping("/items")
    public List<PendingItem> getAllItems() {
        return repository.findAll();
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<PendingItem> getItemById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public PendingItem createItem(@Valid @RequestBody PendingItem item) {
        item.setId(null);
        return repository.save(item);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<PendingItem> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody PendingItem updatedItem) {

        return repository.findById(id)
                .map(item -> {
                    item.setTitle(updatedItem.getTitle());
                    item.setWaitingOn(updatedItem.getWaitingOn());
                    item.setDescription(updatedItem.getDescription());
                    item.setStatus(updatedItem.getStatus());
                    item.setInitiatedDate(updatedItem.getInitiatedDate());
                    item.setExpectedResponseDate(updatedItem.getExpectedResponseDate());
                    item.setReminderDate(updatedItem.getReminderDate());

                    PendingItem savedItem = repository.save(item);
                    return ResponseEntity.ok(savedItem);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.ok("Pending item deleted.");
    }
}