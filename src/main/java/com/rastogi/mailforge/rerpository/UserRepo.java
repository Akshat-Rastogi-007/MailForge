package com.rastogi.mailforge.rerpository;

import com.rastogi.mailforge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    User findByMailAddress(String mailAddress);
}
