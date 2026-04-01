package com.nds.gateway.repository;

import com.nds.gateway.entity.NotificationEntity;
import com.nds.shared.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByStatus(NotificationStatus status);

    @Query("SELECT n.channel, COUNT(n) FROM NotificationEntity n GROUP BY n.channel")
    List<Object[]> countByChannel();

    @Query("SELECT n.status, COUNT(n) FROM NotificationEntity n GROUP BY n.status")
    List<Object[]> countByStatus();
}
