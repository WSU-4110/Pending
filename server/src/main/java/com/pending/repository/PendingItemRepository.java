package com.pending.repository;

import com.pending.model.PendingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendingItemRepository extends JpaRepository<PendingItem, Long> {

    List<PendingItem> findByOwnerId(Long ownerID);

    Optional<PendingItem> findByIdAndOwnerId(Long id, Long ownerID);

    boolean existsByIdAndOwnerId(Long id, Long ownerID);

    Optional<PendingItem> findByIdAndOwnerIdAndStatus(Long id, Long ownerId, Integer status);

    Optional<PendingItem> findByIdAndOwnerIdAndResolvedAtIsNotNull(Long id, Long ownerId);

    Optional<PendingItem> findByIdAndOwnerIdAndDeletedAtIsNotNull(Long id, Long ownerId);

}