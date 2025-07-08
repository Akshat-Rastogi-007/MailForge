package com.rastogi.mailforge.service.email;

import com.rastogi.mailforge.dto.EmailDto;
import com.rastogi.mailforge.entity.Email;
import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.repository.EmailRepo;
import com.rastogi.mailforge.repository.UserRepo;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public String sendMail(EmailDto emailDto) {
        String result;
        try{
            Email map = modelMapper.map(emailDto, Email.class);
            Optional<User> from = userRepo.findByMailAddress(map.getFrom());
            if (from.isPresent()) {
                from.get().getSentMails().add(map);
                userRepo.save(from.get());
            }
            Optional<User> to = userRepo.findByMailAddress(map.getTo());
            if (to.isPresent()) {
                to.get().getReceivedMails().add(map);
                userRepo.save(to.get());
            }
            map.setSentAt(LocalDateTime.now().toString());
            map.setStatus("SENT");
            emailRepo.save(map);
            log.info("Sent mail at: {}", map.getSentAt());
            result = "Mail Sent";
        }
        catch(Exception e){
            result = e.getMessage();
            log.error("Cannot send mail due to {}",e.getMessage());
        }
        return result;
    }


}
