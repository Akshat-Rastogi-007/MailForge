package com.rastogi.mailforge.service.otp;


import com.rastogi.mailforge.entity.OtpEntry;
import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.repository.OtpRepository;
import com.rastogi.mailforge.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpValidation {

    private static final Logger log = LoggerFactory.getLogger("message");


    private final UserRepo userRepo;
    private final OtpRepository otpRepository;

    public OtpValidation(UserRepo userRepo, OtpRepository otpRepository) {
        this.userRepo = userRepo;
        this.otpRepository = otpRepository;
    }

    public String validateOtp(String inputOtp, String userId) {
        String result;
        try {
            Optional<OtpEntry> otpEntry = otpRepository.findByUserId(userId);

            if (otpEntry.isEmpty()) {
                log.info("Otp entry not found for userId {}", userId);
                return "Otp entry not found";
            }

            OtpEntry entry = otpEntry.get();

            if (LocalDateTime.now().isAfter(entry.getExpiresAt())) {
                log.info("Otp expired for userId {}", userId);
                otpRepository.deleteById(userId); // cleanup expired
                return "Otp expired";
            }

            boolean isValid = entry.getOtp().equals(inputOtp);

            if (isValid) {
                Optional<User> userOptional = userRepo.findById(userId);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setVerified(true);
                    user.setPhone(entry.getPhone());
                    userRepo.save(user);
                    log.info("Otp validated for userId {}", userId);
                    result = "Otp validated";
                    otpRepository.deleteById(userId); // use once
                } else {
                    log.warn("User not found with userId {}", userId);
                    result = "User not found";
                }
            } else {
                log.info("Otp is invalid for userId {}", userId);
                result = "Otp is invalid";
            }

        } catch (Exception e) {
            result = "Otp validation failed";
            log.error("Otp validation failed for userId {} due to {}", userId, e.getMessage());
        }
        return result;
    }
}
