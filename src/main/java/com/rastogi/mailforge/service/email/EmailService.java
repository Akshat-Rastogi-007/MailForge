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
                result = "Check sender mail address, no such mail found";
            } else {
                User receiver = receiverOptional.get();

                PublicKey senderPublicKey = KeyGeneration.decodePublicKey(sender.getPublicKey());
                PublicKey receiverPublicKey = KeyGeneration.decodePublicKey(receiver.getPublicKey());

                SecretKey aesKey = KeyGeneration.generateAESKey();
                String encryptedSubject = KeyGeneration.encryptAES(emailDto.getSubject(), aesKey);
                String encryptedBody = KeyGeneration.encryptAES(emailDto.getBody(), aesKey);

                String wrappedKeyForSender = KeyGeneration.encryptAESKey(aesKey, senderPublicKey);
                String wrappedKeyForReceiver = KeyGeneration.encryptAESKey(aesKey, receiverPublicKey);

                Email email = modelMapper.map(emailDto, Email.class);
                email.setId("EMAIL" + sender.getMailAddress().substring(0, sender.getMailAddress().indexOf("@")) + ids++);
                email.setSender(sender);
                email.setReceiver(receiver);
                email.setSubject(encryptedSubject);
                email.setBody(encryptedBody);
                email.setWrappedKeyForSender(wrappedKeyForSender);
                email.setWrappedKeyForReceiver(wrappedKeyForReceiver);
                email.setSentAt(LocalDateTime.now().toString());
                email.setRead(false);
                email.setDeleted(false);
                emailRepo.save(email);
                userRepo.save(sender);
                userRepo.save(receiver);
                log.info("Encrypted email sent from {} to {}", sender.getMailAddress(), receiverMail);
                result = "Email sent successfully";
            }
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


}
