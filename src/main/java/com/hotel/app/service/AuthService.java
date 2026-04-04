package com.hotel.app.service;

import com.hotel.app.entity.User;
import com.hotel.app.repository.CustomerRepository;
import com.hotel.app.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository; // Added dependency

    public AuthService(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<User> login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }


    public boolean register(String email, String password, String name, String phoneCountryCode, String phoneNumber) {
        String passwordHash = hashPassword(password);

        long newUserID = userRepository.save(email, passwordHash, name, phoneCountryCode, phoneNumber, true);

        if (newUserID == -1) {
            return false;
        }

        userRepository.assignRole(newUserID, "ROLE_CUSTOMER");

        customerRepository.createFromSignup(newUserID);

        return true;
    }

    public boolean emailExists(String email) {
        return userRepository.emailExists(email);
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public List<String> getRolesForUser(long userId) {
        return userRepository.findRolesByUserId(userId);
    }
}