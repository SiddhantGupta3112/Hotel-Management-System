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



    public boolean registerCustomer(String email, String password, String name,
                                    String phoneCountryCode, String phoneNumber) {
        if (emailExists(email)) return false;

        String passwordHash = hashPassword(password);

        long newUserId = userRepository.save(email, passwordHash, name,
                phoneCountryCode, phoneNumber, true);

        if (newUserId == -1) return false;

        userRepository.assignRole(newUserId, "ROLE_CUSTOMER");

        // Automatically create the CUSTOMERS profile row so customerId is
        // available immediately after signup via CustomerRepository.findByUserId()
        return customerRepository.createProfile(newUserId);
    }



    public boolean registerStaff(String email, String password, String name,
                                 String phoneCountryCode, String phoneNumber,
                                 long deptId, long managerId,
                                 String jobDesc, double salary) {
        if (emailExists(email)) return false;

        String passwordHash = hashPassword(password);

        long newUserId = userRepository.save(email, passwordHash, name,
                phoneCountryCode, phoneNumber, true);

        if (newUserId == -1) return false;

        userRepository.assignRole(newUserId, "ROLE_STAFF");
        return staffRepository.save(newUserId, deptId, managerId, jobDesc, salary);
    }


    public boolean updateStaffProfessionalDetails(long staffId, long deptId,
                                                  long managerId, String jobDesc,
                                                  double salary) {
        return staffRepository.updateStaffDetails(staffId, deptId, managerId, jobDesc, salary);
    }



    public boolean registerManager(String email, String password, String name,
                                   String phoneCountryCode, String phoneNumber,
                                   long deptId, Long reportsToId,
                                   String jobDesc, double salary) {
        if (emailExists(email)) return false;

        String passwordHash = hashPassword(password);

        long newUserId = userRepository.save(email, passwordHash, name,
                phoneCountryCode, phoneNumber, true);

        if (newUserId == -1) return false;

        userRepository.assignRole(newUserId, "ROLE_MANAGER");
        return managerRepository.save(newUserId, deptId, reportsToId, jobDesc, salary);
    }


    public boolean updateManagerProfessionalDetails(long managerId, long deptId,
                                                    Long reportsToId, String jobDesc,
                                                    double salary) {
        return managerRepository.updateManagerDetails(managerId, deptId,
                reportsToId, jobDesc, salary);
    }



    public boolean updatePassword(long userId, String newPassword) {
        String passwordHash = hashPassword(newPassword);
        return userRepository.updatePassword(userId, passwordHash);
    }

    public boolean requestAccountDeletion(long userId) {
        return userRepository.deleteUserAccount(userId);
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