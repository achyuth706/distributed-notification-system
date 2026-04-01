package com.nds.notification.repository;

import com.nds.notification.entity.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadLetterRepository extends JpaRepository<DeadLetterEvent, String> {
}
