package com.example.usersservice.service;

import com.example.usersservice.model.User;
import com.example.usersservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NextSequenceService NextSequenceService;
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(String userId) {
        return userRepository.findById(userId);
    }

    public String deleteUser(String userId) {
        if(!userRepository.findById(userId).isPresent()) {
            throw new IllegalStateException("User not found");

        }
        else {
            userRepository.deleteById(userId);
            return "user1 deleted";
        }

    }

    public User updateUser(User user,String userId) {
        User user1 = userRepository.findById(userId).orElse(null);
        if(user1!=null) {
            user1.setFirstName(user.getFirstName());
            user1.setLastName(user.getLastName());
            if(userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new IllegalStateException("User with this email already exists");
            }
            user1.setEmail(user.getEmail());
            user1.setPassword(user.getPassword());
            userRepository.save(user1);
            return user1;
        }
        else
        return null;
    }

    public User createUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent())
            throw new IllegalStateException("User with this email already exists");
        return userRepository.save(new User(Integer.toString(NextSequenceService.getNextSequence("users_sequence")),
                                            user.getFirstName(),
                                            user.getLastName(),
                                            user.getEmail(),
                                            user.getPassword()));
    }

    public String login(Object credentials) {
        return "logged in";
    }
}
