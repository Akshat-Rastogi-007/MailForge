package com.rastogi.mailforge.service.user;

import com.rastogi.mailforge.dto.UserDto;
import com.rastogi.mailforge.dto.respose.UserResponseDto;
import com.rastogi.mailforge.entity.User;
import com.rastogi.mailforge.rerpository.UserRepo;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserCrudService {

    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger log = LoggerFactory.getLogger("usercreation");

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
            map.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
            userRepo.save(map);
            log.info("Created user with mail address {}", map.getMailAddress());
            result = "User Created Successfully";
        } catch (Exception e) {
            log.error("Created user with mail address {}", userDto.getMailAddress());
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
            User byMailAddress = userRepo.findByMailAddress(newMailAddress);
            byMailAddress.setMailAddress(newMailAddress);
            userRepo.save(byMailAddress);
            result = "Saved";
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



}
