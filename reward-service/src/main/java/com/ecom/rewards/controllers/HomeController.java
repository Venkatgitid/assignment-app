package com.ecom.rewards.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {

    @GetMapping("/")
    public String home(){
        log.info("Home Page Called.");
        return "Home page";
    }

    @GetMapping("/admin/home")
    public String getAdminHome(){
        return "Admin Home page";
    }

    @GetMapping("/customer/home")
    public String getCustomerHome(){
        return "Customer Home page";
    }
}
