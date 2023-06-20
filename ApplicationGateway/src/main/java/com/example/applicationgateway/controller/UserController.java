package com.example.applicationgateway.controller;

import com.example.applicationgateway.dto.LoginRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return "Login";
    }
    @GetMapping("/restricted")
    public String restrictedTest(@RequestBody LoginRequest loginRequest) {
        return "Restricted";
    }

}
