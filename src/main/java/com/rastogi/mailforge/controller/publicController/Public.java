package com.rastogi.mailforge.controller.publicController;


import com.rastogi.mailforge.dto.UserDto;
import com.rastogi.mailforge.dto.request.PhoneVerificationDto;
import com.rastogi.mailforge.service.user.UserCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/")
public class Public {

    private final UserCrudService userCrudService;
    private static final Logger log = LoggerFactory.getLogger("usercreation");

    public Public(UserCrudService userCrudService) {
        this.userCrudService = userCrudService;
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try{
            Boolean b = userCrudService.doesUserExist(userDto.getMailAddress() + "@mailforge.local");
            if (b){
                log.error("User with mail address {} already exists", userDto.getMailAddress());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mail Address already exists");
            }
            String result = userCrudService.createUser(userDto);
            if (result.equals("User created successfully")){
                log.info("User created successfully with mail address {}", userDto.getMailAddress());
                return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        catch (Exception e){
            log.error("Error creating user {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
