package com.ecom.rewards.controllers;

import com.ecom.rewards.models.AppUser;
import com.ecom.rewards.dto.LoginDto;
import com.ecom.rewards.dto.RegisterDto;
import com.ecom.rewards.repositories.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import static com.ecom.rewards.utils.ControllerUtils.getErrorResponseEntity;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDto registerDto,
                                           BindingResult result){
        log.info("registering user...");
        if (result.hasErrors()) {
            return getErrorResponseEntity(result);
        }

        var bCryptPasswordEncoder = new BCryptPasswordEncoder();
        AppUser appUser = new AppUser();
        appUser.setFirstName(registerDto.getFirstName());
        appUser.setLastName(registerDto.getLastName());
        appUser.setUserName(registerDto.getUserName());
        appUser.setEmail(registerDto.getEmail());
        appUser.setRole("client");
        appUser.setCreatedAt(new Date());
        appUser.setPassword(bCryptPasswordEncoder.encode(registerDto.getPassword()));

        try {
            //check if username / email is already used or not
            var existingUser = appUserRepository.findByUserName(registerDto.getUserName());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body("Username already exists.");
            }

            existingUser = appUserRepository.findByEmail(registerDto.getEmail());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body("Email address already used.");
            }

            //save the user into db
            log.info("saving the new user details...");
            appUserRepository.save(appUser);

            //create jwtToken for the user
            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("user", appUser);
            response.put("token", jwtToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("There is an exception in registration :",e);
        }

        return ResponseEntity.badRequest().body("Error");
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

            //if auth is success generate webtoken
            AppUser appUser = appUserRepository.findByUserName(loginDto.getUserName());
            String jwtToken = createJwtToken(appUser);
            var response = new HashMap<String, Object>();
            response.put("user", appUser);
            response.put("token", jwtToken);

            return ResponseEntity.ok(response);
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
        AppUser appUser = appUserRepository.findByUserName(authentication.getName());
        response.put("User", appUser);
        return ResponseEntity.ok(response);
    }

    private String createJwtToken(AppUser appUser) {
        log.info("generating user web token...");
        var now = Instant.now(Clock.system(ZoneId.of("UTC")));
        var claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(24 * 3600))
                .subject(appUser.getUserName())
                .claim("role", appUser.getRole())
                .build();
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getBytes()));
        var params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims
        );
        return encoder.encode(params).getTokenValue();
    }

}
