package com.rastogi.mailforge.service.email;

import com.rastogi.mailforge.dto.EmailDto;
import com.rastogi.mailforge.dto.respose.MailResponseDto;
import com.rastogi.mailforge.entity.Email;
import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.repository.EmailRepo;
import com.rastogi.mailforge.repository.UserRepo;
import com.rastogi.mailforge.security.UserDetailImpl;
import com.rastogi.mailforge.config.KeyGeneration;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmailService {

    public static final Logger log = LoggerFactory.getLogger("emailcreation");
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;
    private final EmailRepo emailRepo;

    public EmailService(ModelMapper modelMapper, UserRepo userRepo, EmailRepo emailRepo) {
        this.modelMapper = modelMapper;
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
    }

    private static int ids;

    public String sendToLocalMail(EmailDto emailDto) {
        String result;
        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User sender = principal.getUser();
            String receiverMail = emailDto.getReceiverAddress();
            Optional<User> receiverOptional = userRepo.findByMailAddress(receiverMail);

            if (receiverOptional.isEmpty()) {
                log.warn("No receiver address found for {}", receiverMail);
                return "Check receiver mail address, no such mail found";
            }

            User receiver = receiverOptional.get();
            String emailId = "EMAIL" + sender.getMailAddress().split("@")[0] + ids++;

            Email email = encryptAndPrepareEmail(emailDto, sender, receiver, emailId);
            email.setSentAt(LocalDateTime.now());
            email.setRead(false);
            email.setDeleted(false);
            email.setStatus("SENT");

            emailRepo.save(email);
            userRepo.save(sender);
            userRepo.save(receiver);

            log.info("Encrypted email sent from {} to {}", sender.getMailAddress(), receiverMail);
            result = "Email sent successfully";
        }
        catch (Exception e) {
            result = "Something went wrong while sending email";
            log.error("Error sending email to {} due to {}", emailDto.getReceiverAddress(), e.getMessage());
        }
        return result;
    }

    public List<MailResponseDto> getSentMail() {
        List<MailResponseDto> result = new ArrayList<>();

        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();

            PrivateKey privateKey = KeyGeneration.decodePrivateKey(user.getPrivateKey());

            List<Email> sentMails = user.getSentMails();

            for (Email email : sentMails) {
                if(email.isDeleteForSender()) {
                    MailResponseDto dto = decryptMail(email, privateKey, true, user.getMailAddress());
                    if (dto != null) result.add(dto);
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to load sent mail: {}", e.getMessage());
        }

        return result;
    }

    public List<MailResponseDto> getInboxMail() {
        List<MailResponseDto> result = new ArrayList<>();

        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();

            PrivateKey privateKey = KeyGeneration.decodePrivateKey(user.getPrivateKey());

            List<Email> receivedMails = user.getReceivedMails();

            for (Email email : receivedMails) {
                if(!email.isDeleteForReceiver()) {
                    MailResponseDto dto = decryptMail(email, privateKey, false, user.getMailAddress());
                    if (dto != null) result.add(dto);
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to load inbox: {}", e.getMessage());
        }

        return result;
    }

    public List<MailResponseDto> getAllMails() {
        List<MailResponseDto> result = new ArrayList<>();
        try {
            List<MailResponseDto> inboxMail = getInboxMail();
            List<MailResponseDto> sentMail = getSentMail();
            result.addAll(inboxMail);
            result.addAll(sentMail);
            log.info("Successfully loaded all mails");
            return result;
        } catch (Exception e) {
            log.error("Failed to load all mails: {}", e.getMessage());
        }
        return result;
    }

    public List<MailResponseDto> getAllSoftDeletedMail() {
        List<MailResponseDto> allDeletedMails = new ArrayList<>();
        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();

            PrivateKey privateKey = KeyGeneration.decodePrivateKey(user.getPrivateKey());

            List<Email> receivedMails = user.getReceivedMails();
            List<Email> sentMails = user.getSentMails();

            for (Email email : receivedMails) {
                if (email.isDeleteForReceiver()) {
                    MailResponseDto dto = decryptMail(email, privateKey, false, user.getMailAddress());
                    if (dto != null) allDeletedMails.add(dto);
                }
            }

            for (Email email : sentMails) {
                if (email.isDeleteForSender()) {
                    MailResponseDto dto = decryptMail(email, privateKey, true, user.getMailAddress());
                    if (dto != null) allDeletedMails.add(dto);
                }
            }

        }
        catch (Exception e) {
            log.error("Failed to load deleted mails: {}", e.getMessage());
        }
        return allDeletedMails;
    }

    private MailResponseDto decryptMail(Email email, PrivateKey privateKey, boolean isSender, String currentUserAddress) {
        try {
            SecretKey aesKey = KeyGeneration.decryptAESKey(
                    isSender ? email.getWrappedKeyForSender() : email.getWrappedKeyForReceiver(),
                    privateKey
            );

            String decryptedSubject = KeyGeneration.decryptAES(email.getSubject(), aesKey);
            String decryptedBody = KeyGeneration.decryptAES(email.getBody(), aesKey);

            MailResponseDto mailDto = new MailResponseDto();
            mailDto.setSenderAddress(isSender ? currentUserAddress : email.getSender().getMailAddress());
            mailDto.setReceiverAddress(isSender ? email.getReceiver().getMailAddress() : currentUserAddress);
            mailDto.setSubject(decryptedSubject);
            mailDto.setBody(decryptedBody);
            mailDto.setSentAt(email.getSentAt());

            return mailDto;

        } catch (Exception e) {
            log.warn("Failed to decrypt {} mail with id {}: {}",
                    isSender ? "sent" : "inbox",
                    email.getId(),
                    e.getMessage());
            return null;
        }
    }

    public String softDeleteMail(String mailId) {
        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();

            List<Email> receivedMails = user.getReceivedMails();
            for (Email email : receivedMails) {
                if (email.getId().equals(mailId)) {
                    email.setDeleteForReceiver(true);
                    emailRepo.save(email);
                    log.info("Soft deleted mail (receiver side) with id {}", mailId);
                    return "Successfully deleted mail from inbox.";
                }
            }

            List<Email> sentMails = user.getSentMails();
            for (Email email : sentMails) {
                if (email.getId().equals(mailId)) {
                    email.setDeleteForSender(true);
                    emailRepo.save(email);
                    log.info("Soft deleted mail (sender side) with id {}", mailId);
                    return "Successfully deleted mail from sent.";
                }
            }

            log.warn("No mail found with id {}", mailId);
            return "No such mail found.";

        } catch (Exception e) {
            log.error("Failed to soft delete mail with id {}: {}", mailId, e.getMessage());
            return "Failed to delete mail.";
        }
    }

    public String undoDeleteMail(String mailId) {
        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();

            List<Email> receivedMails = user.getReceivedMails();
            for (Email email : receivedMails) {
                if (email.getId().equals(mailId)) {
                    email.setDeleteForReceiver(false);
                    emailRepo.save(email);
                    log.info("Mail has been restored (receiver side) with id {}", mailId);
                    return "Mail has been restored";
                }
            }

            List<Email> sentMails = user.getSentMails();
            for (Email email : sentMails) {
                if (email.getId().equals(mailId)) {
                    email.setDeleteForSender(false);
                    emailRepo.save(email);
                    log.info("Mail has been restored (sender side) with id {}", mailId);
                    return "Mail has been restored";
                }
            }

            log.warn("No mail found with id {} while restoring mail", mailId);
            return "No such mail found.";

        } catch (Exception e) {
            log.error("Failed to restore mail with id {}: {}", mailId, e.getMessage());
            return "Failed to restore mail.";
        }
    }

    public String permanentDeleteMail(String mailId) {
        try {
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principal.getUser();
            List<Email> receivedMails = user.getReceivedMails();
            for (Email email : receivedMails) {
                if (email.getId().equals(mailId)) {
                   emailRepo.delete(email);
                    log.info("Mail has been DELETED with id {} (RECEIVED MAIL)", mailId);
                    return "Mail has been DELETED";
                }
            }

            List<Email> sentMails = user.getSentMails();
            for (Email email : sentMails) {
                if (email.getId().equals(mailId)) {
                    emailRepo.delete(email);
                    log.info("Mail has been DELETED with id {} (SENT MAIL)", mailId);
                    return "Mail has been DELETED";
                }
            }
            log.warn("No mail found with id {} while DELETING mail", mailId);
            return "No such mail found.";
        }
        catch (Exception e) {
            log.error("Failed to DELETE mail with id {}: {}", mailId, e.getMessage());
            return "Failed to DELETE mail.";
        }
    }

    public String saveEmail(EmailDto emailDto) {
        try{
            UserDetailImpl principal = (UserDetailImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User sender = principal.getUser();
            Optional<User> byMailAddress = userRepo.findByMailAddress(emailDto.getReceiverAddress());
            if(byMailAddress.isEmpty()) {
                log.warn("No mail address found with id {}", emailDto.getReceiverAddress());
                return "Check receiver mail address, no such mail found";
            }
            User receiver = byMailAddress.get();
            String emailId = "EMAIL" + sender.getMailAddress().split("@")[0] + ids++;
            Email email = encryptAndPrepareEmail(emailDto, sender, receiver, emailId);
            sender.getSentMails().add(email);
            email.setSender(sender);
            email.setReceiver(receiver);
            email.setSentAt(emailDto.getScheduledTime());
            email.setStatus("PENDING");
            userRepo.save(sender);
            emailRepo.save(email);
            return "Email saved successfully";
        }
        catch (Exception e) {
            log.error("Failed to save email with id {}: {}", emailDto.getReceiverAddress(), e.getMessage());
            return "Failed to save email.";
        }
    }

    @Scheduled(fixedRate = 60000)
    public String scheduledTimeSent() {
        LocalDateTime now = LocalDateTime.now();
        List<Email> pendingEmails = emailRepo.findByStatusAndScheduledTimeLessThanEqual("PENDING", now);

        if (pendingEmails.isEmpty()) {
            return "No pending emails found";
        }

        int successCount = 0;
        int failCount = 0;

        for (Email email : pendingEmails) {
            try {
                User receiver = email.getReceiver();
                receiver.getReceivedMails().add(email);

                email.setStatus("SENT");

                emailRepo.save(email);
                userRepo.save(receiver);

                log.info("Scheduled email {} sent to {}", email.getId(), receiver.getMailAddress());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send scheduled mail {}: {}", email.getId(), e.getMessage());
                failCount++;
            }
        }

        return "Scheduled email processing complete. Success: " + successCount + ", Failed: " + failCount;
    }


    public Email encryptAndPrepareEmail(EmailDto emailDto, User sender, User receiver, String emailId) throws Exception {

        PublicKey senderPublicKey = KeyGeneration.decodePublicKey(sender.getPublicKey());
        PublicKey receiverPublicKey = KeyGeneration.decodePublicKey(receiver.getPublicKey());

        SecretKey aesKey = KeyGeneration.generateAESKey();
        String encryptedSubject = KeyGeneration.encryptAES(emailDto.getSubject(), aesKey);
        String encryptedBody = KeyGeneration.encryptAES(emailDto.getBody(), aesKey);

        String wrappedKeyForSender = KeyGeneration.encryptAESKey(aesKey, senderPublicKey);
        String wrappedKeyForReceiver = KeyGeneration.encryptAESKey(aesKey, receiverPublicKey);

        Email email = modelMapper.map(emailDto, Email.class);
        email.setId(emailId);
        email.setSender(sender);
        email.setReceiver(receiver);
        email.setSubject(encryptedSubject);
        email.setBody(encryptedBody);
        email.setWrappedKeyForSender(wrappedKeyForSender);
        email.setWrappedKeyForReceiver(wrappedKeyForReceiver);


        return email;
    }

}
