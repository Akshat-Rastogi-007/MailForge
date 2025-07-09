package com.rastogi.mailforge.service.messagingService;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger("message");


    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.from_number}")
    private String fromNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }


    public String sendMessage(String toPhoneNumber, String otp) {
        String result;
        try {
            String messageBody = "Dear MailForge user,\n Your OTP is " + otp + ". It is valid for 5 minutes. Do not share this code.";

            Message message = Message.creator(
                            new PhoneNumber(toPhoneNumber),
                            new PhoneNumber(fromNumber), messageBody)
                    .create();
            log.info("Message sent to {}", toPhoneNumber);
            result = "Message sent";
        }
        catch (com.twilio.exception.ApiException e) {
            if (e.getCode() == 21211) {
                log.error("Invalid phone number: {}", toPhoneNumber);
                result = "Invalid phone number";
            } else {
                log.error("Twilio API error: {}", e.getMessage());
                result = "SMS failed: " + e.getMessage();
            }
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            result = "Something went wrong while sending the SMS.";
        }
        return result;
    }

}
