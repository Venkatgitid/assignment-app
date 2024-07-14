package com.ecom.rewards.services;

import com.ecom.rewards.models.AppUser;
import com.ecom.rewards.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

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
}
