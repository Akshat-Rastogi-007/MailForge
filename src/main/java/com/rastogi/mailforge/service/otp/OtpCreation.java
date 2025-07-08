package com.rastogi.mailforge.service.otp;

import com.rastogi.mailforge.entity.OtpEntry;
import com.rastogi.mailforge.repository.OtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpCreation {

    private final OtpRepository otpRepository;
    private final Random random = new Random();

    public OtpCreation(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    private static Logger log = LoggerFactory.getLogger("message");


    public String generateOtp(String userId) {
        String result;
        try {
            String otp = String.format("%06d", random.nextInt(999999)); // 6-digit OTP
            OtpEntry otpEntry = new OtpEntry();
            otpEntry.setOtp(otp);
            otpEntry.setUserId(userId);
            otpEntry.setCreatedAt(LocalDateTime.now());
            otpEntry.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            otpRepository.save(otpEntry);
            log.info("OTP created: {}", otp);
            result = otp;
        } catch (Exception e) {
            result = "OTP creation failed";
            log.error("OTP creation failed due to {}", e.getMessage());
        }
        return result;
    }
}
