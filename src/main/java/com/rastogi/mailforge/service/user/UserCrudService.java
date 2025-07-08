package com.rastogi.mailforge.service.user;

import com.rastogi.mailforge.dto.UserDto;
import com.rastogi.mailforge.dto.respose.UserResponseDto;
import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.repository.UserRepo;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserCrudService {

    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger log = LoggerFactory.getLogger("usercreation");

    static int ids= 0;
    public UserCrudService(UserRepo userRepo, ModelMapper modelMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String createUser(UserDto userDto) {
        String result;
        try {
            User map = modelMapper.map(userDto, User.class);
            // here phone verification will take place
            map.setMailAddress(map.getMailAddress() + "@mailforge.local");
            map.setId("USER-"+ map.getMailAddress() + ++ids);
            map.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
            map.getRole().add("ROLE_USER");
            userRepo.save(map);
            log.info("Created user with mail address {}", map.getMailAddress());
            result = "User Created Successfully";
        } catch (Exception e) {
            log.error("Error while creating user with mail address {}", userDto.getMailAddress());
            result = "Error while creating user";
        }
        return result;
    }

    public List<UserResponseDto>  getAllUsers() {
        List<UserResponseDto> allUsers = new ArrayList<>();
        try{
            List<User> users = userRepo.findAll();
            for (User user : users) {
                UserResponseDto map = modelMapper.map(user, UserResponseDto.class);
                allUsers.add(map);
            }
            log.info("Retrieved all users successfully");
            return allUsers;
        }
        catch (Exception e){
            log.error("Fetching all users failed due to {}", e.getMessage());
        }
        return allUsers;
    }

    public String changeMailAddress(String newMailAddress) {
        String result;
        try {
            Optional<User> byMailAddress = userRepo.findByMailAddress(newMailAddress);
            if (byMailAddress.isPresent()) {
                byMailAddress.get().setMailAddress(newMailAddress);
                userRepo.save(byMailAddress.get());
                result = "Saved";
            }
            else {
                result = "User with mail address " + newMailAddress + " not found";
            }
        }
        catch (Exception e){
            result = "Error while changing user mail address";
            log.error("Changing email address failed due to {}", e.getMessage());
        }
        return result;
    }

//    public String changePassword(String password){
//        String result;
//        try{
//            SecurityContext context = SecurityContextHolder.getContext();
//            Authentication authentication = context.getAuthentication();
//            UserDetails principal = (UserDetails) authentication.getPrincipal();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public Boolean doesUserExist(String mailAddress) {
        try{
            Optional<User> byMailAddress = userRepo.findByMailAddress(mailAddress);
            if (byMailAddress.isPresent()) {
                log.info("User with mail address {} exists", mailAddress);
                return true;
            }
            else {
                log.info("User with mail address {} not found", mailAddress);
                return false;
            }
        }
        catch (Exception e){
            log.error("User checking failed due to {}", e.getMessage());
        }
        return false;
    }


}
