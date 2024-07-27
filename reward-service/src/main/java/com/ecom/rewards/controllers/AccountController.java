package com.ecom.rewards.controllers;

import com.ecom.rewards.dto.LoginDto;
import com.ecom.rewards.dto.RegisterDto;
import com.ecom.rewards.models.AppUser;
import com.ecom.rewards.services.AppUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

import static com.ecom.rewards.utils.ControllerUtils.getErrorResponseEntity;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;

    public AccountController(AppUserService appUserService, AuthenticationManager authenticationManager) {
        this.appUserService = appUserService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDto registerDto,
                                           BindingResult result){
        log.info("registering user...");
        if (result.hasErrors()) {
            return getErrorResponseEntity(result);
        }

        try {
            //check if username / email is already used or not
            var existingUser = appUserService.findByUserName(registerDto.getUserName());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists.");
            }

            existingUser = appUserService.findByEmail(registerDto.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Email address already used.");
            }

            //save the user into db
            HashMap<String, Object> resultDetails = appUserService.userRegistration(registerDto);

            return ResponseEntity.ok(resultDetails);
        } catch (Exception e) {
            log.error("There is an exception in registration :",e);
        }

        return ResponseEntity.badRequest().body("Registration Failed");
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDto loginDto,
                                        BindingResult result ){
        log.info("user tying to login...");
        if (result.hasErrors()) {
            return getErrorResponseEntity(result);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUserName(),
                            loginDto.getPassword()
                    )
            );
            var userWithToken = appUserService.getUserWithToken(loginDto.getUserName());
            return ResponseEntity.ok(userWithToken);
        } catch (Exception e){
            log.error("Exception while login :", e);
        }

        return ResponseEntity.badRequest().body("Invalid UserName and Password Provided.");
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> getProfile(Authentication authentication){
        var response = new HashMap<String, Object>();
        response.put("UserName", authentication.getName());
        response.put("Authorities", authentication.getAuthorities());
        Optional<AppUser> appUser = appUserService.findByUserName(authentication.getName());
        if (appUser.isPresent()) {
            response.put("User", appUser);
        }
        return ResponseEntity.ok(response);
    }

}
