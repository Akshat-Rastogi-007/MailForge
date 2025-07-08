package com.rastogi.mailforge.security;

import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.repository.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {


    private final UserRepo userRepo;

    public CustomUserDetailService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> byMailAddress = userRepo.findByMailAddress(username);
        if (byMailAddress.isPresent()) {
            User user = byMailAddress.get();
            return new UserDetailImpl(user);
        }
        else{
            throw new UsernameNotFoundException(username);
        }
    }
}
