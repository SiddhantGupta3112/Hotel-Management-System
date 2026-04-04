package com.hotel.app.service;

import com.hotel.app.entity.User;
import com.hotel.app.repository.CustomerRepository;
import com.hotel.app.repository.ManagerRepository;
import com.hotel.app.repository.StaffRepository;
import com.hotel.app.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       StaffRepository staffRepository,
                       ManagerRepository managerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.managerRepository = managerRepository;
    }

    // --- Authentication Logic ---

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

    // --- Customer Methods ---

    public boolean registerCustomer(String email, String password, String name,
                                    String phoneCountryCode, String phoneNumber) {
        if (emailExists(email)) return false;

        String passwordHash = hashPassword(password);
        long newUserId = userRepository.save(email, passwordHash, name,
                phoneCountryCode, phoneNumber, true);

        if (newUserId == -1) return false;

        userRepository.assignRole(newUserId, "ROLE_CUSTOMER");
        return customerRepository.createProfile(newUserId);
    }

    // --- Staff Methods ---

    public boolean registerStaff(String email, String password, String name,
                                 String phoneCountryCode, String phoneNumber,
                                 long deptId, long managerId, String jobDesc, double salary) {
        if (emailExists(email)) return false;

        String passwordHash = hashPassword(password);
        long newUserId = userRepository.save(email, passwordHash, name,
                phoneCountryCode, phoneNumber, true);

        if (newUserId == -1) return false;

        userRepository.assignRole(newUserId, "ROLE_STAFF");
        return staffRepository.save(newUserId, deptId, managerId, jobDesc, salary);
    }

    public boolean updateStaffProfessionalDetails(long staffId, long deptId, long managerId,
                                                  String jobDesc, double salary) {
        return staffRepository.updateStaffDetails(staffId, deptId, managerId, jobDesc, salary);
    }

    // --- Manager Methods ---

    public boolean registerManager(String email, String password, String name,
                                   String countryCode, String phone,
                                   long deptId, Long reportsToId, String jobDesc, double salary) {

        if (userRepository.emailExists(email)) return false;

        String hash = hashPassword(password);

        // Step 1: Save User (Ensure your UserRepo returns the sequence ID!)
        long userId = userRepository.save(email, hash, name, countryCode, phone, true);
        if (userId == -1) return false;

        // Step 2: Role Assignment
        userRepository.assignRole(userId, "ROLE_MANAGER");

        // Step 3: Manager Profile
        // Passing reportsToId directly (can be null)
        return managerRepository.save(userId, deptId, reportsToId, jobDesc, salary);
    }

    public boolean updateManagerProfessionalDetails(long managerId, long deptId, Long reportsToId,
                                                    String jobDesc, double salary) {
        return managerRepository.updateManagerDetails(managerId, deptId, reportsToId, jobDesc, salary);
    }

    // --- Shared Account Utility Methods ---

    public boolean requestAccountDeletion(long userId) {
        return userRepository.deleteUserAccount(userId);
    }

    public boolean updatePassword(Long userId, String password) {
        String password_hash = hashPassword(password);
        return userRepository.updatePassword(userId, password_hash);
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