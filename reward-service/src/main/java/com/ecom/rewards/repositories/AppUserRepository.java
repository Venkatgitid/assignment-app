package com.ecom.rewards.repositories;

import com.ecom.rewards.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    AppUser findByUserName(String userName);
    AppUser findByEmail(String email);
}
