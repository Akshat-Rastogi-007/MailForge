package com.rastogi.mailforge.controller.userController;

import com.rastogi.mailforge.dto.request.Otp;
import com.rastogi.mailforge.dto.request.PhoneVerificationDto;
import com.rastogi.mailforge.security.UserDetailImpl;
import com.rastogi.mailforge.service.messagingService.MessagingService;
import com.rastogi.mailforge.service.otp.OtpCreation;
import com.rastogi.mailforge.service.otp.OtpValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger("message");

    private final OtpCreation otpCreation;
    private final OtpValidation otpValidation;
    private final MessagingService messagingService;

    public AccountController(OtpCreation otpCreation, OtpValidation otpValidation, MessagingService messagingService) {
        this.otpCreation = otpCreation;
        this.otpValidation = otpValidation;
        this.messagingService = messagingService;
    }

    @PostMapping("/add-phone")
    public ResponseEntity<?> verifyPhone(@RequestBody PhoneVerificationDto phoneVerificationDto) {
        try {
            UserDetailImpl userDetailImpl = (UserDetailImpl)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String id = userDetailImpl.getUser().getId();
            String phone = phoneVerificationDto.getPhone();
            String otp = otpCreation.generateOtp(id, phone);
            if(otp.equals("OTP creation failed")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            String result = messagingService.sendMessage(phone, otp);
            if(result.equals("Invalid phone number")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone number. Please check the number and try again.");
            }
            else if (result.equals("Message sent")){
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        catch (Exception e) {
            log.error("Error occurred {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error, Please try again later.");
        }
    }

    @GetMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Otp otp) {
        try {
            UserDetailImpl userDetailImpl = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String id = userDetailImpl.getUser().getId();

            String result = otpValidation.validateOtp(otp.getOtp(), id);

            return switch (result) {
                case "Otp validated" -> ResponseEntity.ok("Phone Number Verified");
                case "Otp is invalid" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP is invalid");
                case "Otp expired" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP has expired");
                case "Otp entry not found" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body("No OTP found for this user");
                case "User not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OTP validation failed");
            };

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong: " + e.getMessage());
        }
    }

}
