package com.ecom.rewards.services;

import com.ecom.rewards.dto.RegisterDto;
import com.ecom.rewards.models.AppUser;
import com.ecom.rewards.repositories.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
public class AppUserService implements UserDetailsService {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUserName(username);
        if (appUser != null) {
            return User.withUsername(appUser.getUserName())
                    .password(appUser.getPassword())
                    .roles(appUser.getRole())
                    .build();
        }
        return null;
    }

    public Optional<AppUser> findByUserName(String userName) {
        AppUser appUser = appUserRepository.findByUserName(userName);
        return Optional.ofNullable(appUser);
    }

    public Optional<AppUser> findByEmail(String email) {
        AppUser appUser = appUserRepository.findByEmail(email);
        return Optional.ofNullable(appUser);
    }

    public HashMap<String, Object> userRegistration(RegisterDto registerDto) {
        var bCryptPasswordEncoder = new BCryptPasswordEncoder();
        AppUser appUser = new AppUser();
        appUser.setFirstName(registerDto.getFirstName());
        appUser.setLastName(registerDto.getLastName());
        appUser.setUserName(registerDto.getUserName());
        appUser.setEmail(registerDto.getEmail());
        appUser.setRole("client");
        appUser.setCreatedAt(new Date());
        appUser.setPassword(bCryptPasswordEncoder.encode(registerDto.getPassword()));

        //save the user into db
        log.info("saving the new user details...");
        AppUser savedUser = appUserRepository.save(appUser);

        //create jwtToken for the user
        String jwtToken = createJwtToken(savedUser);

        var result = new HashMap<String, Object>();
        result.put("user", savedUser);
        result.put("token", jwtToken);

        return result;
    }

    public HashMap<String, Object> getUserWithToken(@NotEmpty String userName) {
        AppUser appUser = appUserRepository.findByUserName(userName);
        String jwtToken = createJwtToken(appUser);
        var result = new HashMap<String, Object>();
        result.put("user", appUser);
        result.put("token", jwtToken);
        return result;
    }

    public String createJwtToken(AppUser appUser) {
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
