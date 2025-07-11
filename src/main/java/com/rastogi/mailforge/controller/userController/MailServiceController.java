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
        List<MailResponseDto> allMail = emailService.getSentMail();
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

    @DeleteMapping("/soft-delete")
    public ResponseEntity<?> softDeleteMail(@RequestParam String mailId){
        String result = emailService.softDeleteMail(mailId);
        if (result.equals("Successfully deleted mail ")) {
            return  new ResponseEntity<>(result, HttpStatus.OK);
        }
        else if (result.equals("No such mail found.")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trash")
    public ResponseEntity<?> getSoftDeletedMail(){
        List<MailResponseDto> allSoftDeletedMail = emailService.getAllSoftDeletedMail();
        if (allSoftDeletedMail.isEmpty()) {
            return new ResponseEntity<>("No mail found", HttpStatus.NOT_FOUND);
        }
        else{
            return new ResponseEntity<>(allSoftDeletedMail, HttpStatus.OK);
        }
    }

    @GetMapping("/all-mails")
    public ResponseEntity<?> getAllMails(){
        List<MailResponseDto> allMails = emailService.getAllMails();
        if (allMails.isEmpty()) {
            return new ResponseEntity<>("No mail found", HttpStatus.NOT_FOUND);
        }
        else{
            return new ResponseEntity<>(allMails, HttpStatus.OK);
        }
    }

    @DeleteMapping("/permanent-delete")
    public ResponseEntity<?> permanentDeleteMail(@RequestParam String mailId){
        String result = emailService.permanentDeleteMail(mailId);
        if (result.equals("Mail has been DELETED")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else if (result.equals("No such mail found.")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/restore-mail")
    public ResponseEntity<?> restoreMail(@RequestParam String mailId){
        String result = emailService.undoDeleteMail(mailId);
        if (result.equals("Mail has been restored")) {
            return new ResponseEntity<>("Mail has been restored", HttpStatus.OK);
        }
        else if (result.equals("No such mail found.")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        else{
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/scheduled-mail")
    public ResponseEntity<?> scheduledMail(@RequestBody EmailDto emailDto) {
        String result = emailService.saveEmail(emailDto);
        if (result.equals("Email saved successfully")) {
            return new ResponseEntity<>("Email will be send on given time", HttpStatus.OK);
        }
        return new ResponseEntity<>("Failed to save email.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
