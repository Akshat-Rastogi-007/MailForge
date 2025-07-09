package com.rastogi.mailforge.controller.publicController;

import com.rastogi.mailforge.dto.respose.UserResponseDto;
import com.rastogi.mailforge.payload.JwtAuthRequest;
import com.rastogi.mailforge.payload.JwtAuthResponse;
import com.rastogi.mailforge.security.JwtToken;
import com.rastogi.mailforge.service.user.UserCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class Login {


    private final AuthenticationManager authenticationManager;
    private final UserCrudService userService;
    private final JwtToken jwtToken;
    private final Logger log = LoggerFactory.getLogger("login");

    public Login(AuthenticationManager authenticationManager, UserCrudService userService, JwtToken jwtToken) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtToken = jwtToken;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtAuthRequest jwtAuthRequest) {
            String emailAddress = jwtAuthRequest.getEmailAddress();
            String password = jwtAuthRequest.getPassword();

        try {
            UserDetails authenticate = authenticate(emailAddress, password);

            if (authenticate == null) {
                log.warn("Username or password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }

            String token = jwtToken.generateMyToken(authenticate);

            UserResponseDto userResponseDto = userService.loginResponse(emailAddress);

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(token,userResponseDto);
            log.info("User {} successfully logged in ", emailAddress);
            return ResponseEntity.ok(jwtAuthResponse);
        } catch (Exception e) {
            log.error("Error {} while loggin user {}",e.getMessage(),emailAddress);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, Try again later");
        }

    }


    private UserDetails authenticate(String email, String password) {

        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authenticate = authenticationManager.authenticate(token);
            log.info("Authenticated user {}", authenticate.getName());
            return (UserDetails) authenticate.getPrincipal();
        }
        catch (Exception e) {
            log.error("Error while authenticating user {}", e.getMessage());
            return null;
        }
    }
}
