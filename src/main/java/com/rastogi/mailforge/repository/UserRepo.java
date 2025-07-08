package com.rastogi.mailforge.repository;

import com.rastogi.mailforge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    Optional<User> findByMailAddress(String mailAddress);
}
