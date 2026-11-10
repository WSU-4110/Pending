package com.pending.controller;

import com.pending.model.PendingItem;
import com.pending.model.Reminder;
import com.pending.repository.PendingItemRepository;
import com.pending.repository.ReminderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderRepository reminderRepository;
    private final PendingItemRepository pendingItemRepository;

    public ReminderController(ReminderRepository reminderRepository, PendingItemRepository pendingItemRepository) {
        this.reminderRepository = reminderRepository;
        this.pendingItemRepository = pendingItemRepository;
    }

    @GetMapping
    public ResponseEntity<List<Reminder>> getAllUserReminders(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reminderRepository.findAllActiveByOwnerId(userId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Reminder>> getReminderByTask(@PathVariable Long taskId, @AuthenticationPrincipal Long userId) {
        if(userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reminderRepository.findByTaskIdAndOwnerId(taskId, userId));
    }

    @PostMapping
    public ResponseEntity<?> createReminder(@Valid @RequestBody Reminder reminder, @AuthenticationPrincipal Long userId) {
        if(userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        Optional<PendingItem> task = pendingItemRepository.findByIdAndOwnerIdAndDeletedAtIsNull(reminder.getTaskId(), userId);

        if(task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found or does not belong to the user.");
        }

        reminder.setId(null);
        reminder.setCreatedAt(OffsetDateTime.now());

        Reminder saved = reminderRepository.save(reminder);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReminder(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        return reminderRepository.findByIdAndOwnerId(id, userId)
                .map(reminder -> {
                    reminderRepository.deleteById(reminder.getId());
                    return ResponseEntity.ok("Reminder deleted.");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
