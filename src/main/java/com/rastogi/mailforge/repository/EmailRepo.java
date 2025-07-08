package com.rastogi.mailforge.repository;

import com.rastogi.mailforge.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepo extends JpaRepository<Email, String> {
}
