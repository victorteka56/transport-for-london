package com.example.usersservice.controllers;

import com.example.usersservice.model.User;
import com.example.usersservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
@GetMapping("/users")
public List<User> getUsers() {
return userService.getUsers();
}

@GetMapping("/users/{userId}")
public Optional<User> getUser(@PathVariable String userId) {
return userService.getUser(userId);
}
@PostMapping("/users")
public User createUser(@RequestBody User user) {
return userService.createUser(user);
}
@PostMapping("/login")
public String login(@RequestBody Object credentials) {
    return userService.login(credentials);
}
@DeleteMapping("/users/{userId}")
public String deleteUser(@PathVariable String userId) {
return userService.deleteUser(userId);
}
@PutMapping("/users/{userId}")
public User updateUser(@RequestBody User user, @PathVariable String userId) {
     return userService.updateUser(user,userId);
    }
}
