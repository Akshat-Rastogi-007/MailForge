package com.rastogi.mailforge.repository;

import com.rastogi.mailforge.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailRepo extends JpaRepository<Email, String> {
    List<Email> findByStatusAndScheduledTimeLessThanEqual(String status, LocalDateTime now);
}
