package com.pending.repository;

import com.pending.model.PendingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingItemRepository extends JpaRepository<PendingItem, Long> {
}