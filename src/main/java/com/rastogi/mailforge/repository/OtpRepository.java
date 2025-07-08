package com.rastogi.mailforge.repository;

import com.rastogi.mailforge.entity.OtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntry, String> {

    Optional<OtpEntry> findByUserId(String userId);
}
