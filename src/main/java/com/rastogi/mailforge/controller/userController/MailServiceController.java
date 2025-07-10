package com.rastogi.mailforge.controller.userController;


import com.rastogi.mailforge.dto.EmailDto;
import com.rastogi.mailforge.dto.respose.MailResponseDto;
import com.rastogi.mailforge.service.email.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user/api/mail")
public class MailServiceController {

    private final EmailService emailService;

    public MailServiceController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("send-mail")
    public ResponseEntity<?> sendMail(@RequestBody EmailDto emailDto) {

        String result = emailService.sendToLocalMail(emailDto);
        if (result.equals("Email sent successfully")) {
            return new ResponseEntity<>("Email sent successfully", HttpStatus.OK);
        } else if (result.equals("Check sender mail address, no such mail found")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Email sent failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sent-mail")
    public ResponseEntity<?> sentMails(){
        List<MailResponseDto> allMail = emailService.getAllMail();
        if (allMail.isEmpty()) {
            return new ResponseEntity<>("No mail found", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(allMail, HttpStatus.OK);
        }
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> inboxMails(){
        List<MailResponseDto> allMail = emailService.getInboxMail();
        if (allMail.isEmpty()) {
            return new ResponseEntity<>("No mail found", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(allMail, HttpStatus.OK);
        }
    }
}
