package com.pending.controller;

import com.pending.model.PendingItem;
import com.pending.repository.PendingItemRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PendingController {

    private final PendingItemRepository repository;

    public PendingController(PendingItemRepository repository) {
        this.repository = repository;
    }

    // Retrieves all pending tasks that belong to the authenticated user
    @GetMapping("/items")
    public List<PendingItem> getAllItems(@AuthenticationPrincipal Long userId) {
        return repository.findByOwnerId(userId);
    }

    // Retrieve a single task that belongs to the authenticated user
    @GetMapping("/items/{id}")
    public ResponseEntity<PendingItem> getItemById(@PathVariable Long id,
                                                   @AuthenticationPrincipal Long userId) {
        return repository.findByIdAndOwnerId(id, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<PendingItem> createItem(@Valid @RequestBody PendingItem item,
                                  @AuthenticationPrincipal Long userId) {
        item.setId(null);
        item.setOwnerId(userId);
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        if(item.getStatus() == null) item.setStatus(0);
        if(item.getPriority() == null) item.setPriority(0);

        PendingItem savedItem = repository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<PendingItem> updateItem(@PathVariable Long id,
                                                  @Valid @RequestBody PendingItem updatedItem,
                                                  @AuthenticationPrincipal Long userId) {

        return repository.findByIdAndOwnerId(id, userId)
                .map(item -> {
                    item.setTitle(updatedItem.getTitle());
                    item.setWaitingOn(updatedItem.getWaitingOn());
                    item.setStartDate(updatedItem.getStartDate());
                    item.setExpectedResponseDate(updatedItem.getExpectedResponseDate());
                    item.setStatus(updatedItem.getStatus());
                    item.setNotes(updatedItem.getNotes());
                    item.setPriority(updatedItem.getPriority());
                    item.setUpdatedAt(updatedItem.getUpdatedAt());
                    item.setDeletedAt(updatedItem.getDeletedAt());

                    // If status is updated, mark as resolved
                    if(updatedItem.getStatus() != null && updatedItem.getStatus() >= 3) {
                        item.setResolvedAt(OffsetDateTime.now());
                    }else{
                        item.setResolvedAt(updatedItem.getResolvedAt());
                    }

                    return ResponseEntity.ok(repository.save(item));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Soft deletes tasks by updating deleted at
    @DeleteMapping("/items/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id,
                                             @AuthenticationPrincipal Long userId) {

        return repository.findByIdAndOwnerId(id, userId)
                .map(item -> {
                    item.setDeletedAt(OffsetDateTime.now());
                    repository.save(item);
                    return ResponseEntity.ok("Pending item marked for deletion");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}