package kr.ac.kopo.service;


import kr.ac.kopo.entity.User;
import kr.ac.kopo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //User 저장 메서드 (DataInitializer에서 필요)
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }


    public void createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (!user.getUsername().equals(userDetails.getUsername()) &&
                            userRepository.existsByUsername(userDetails.getUsername())) {
                        throw new RuntimeException("Username already exists: " + userDetails.getUsername());
                    }

                    if (userDetails.getEmail() != null &&
                            !userDetails.getEmail().equals(user.getEmail()) &&
                            userRepository.existsByEmail(userDetails.getEmail())) {
                        throw new RuntimeException("Email already exists: " + userDetails.getEmail());
                    }

                    user.setUsername(userDetails.getUsername());
                    user.setEmail(userDetails.getEmail());
                    user.setFullName(userDetails.getFullName());
                    user.setRole(userDetails.getRole());
                    user.setEnabled(userDetails.isEnabled());

                    if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void toggleUserStatus(Long id) {
        userRepository.findById(id)
                .ifPresent(user -> {
                    user.setEnabled(!user.isEnabled());
                    userRepository.save(user);
                });
    }

    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(searchTerm.trim());
    }

    public List<User> getUsersByRole(String role) {
        // Role을 String으로 받아서 처리
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .toList();
    }


    public List<User> getEnabledUsers() {
        return userRepository.findByEnabledTrue();
    }


    public List<User> getDisabledUsers() {
        return userRepository.findByEnabledFalse();
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }


    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Long getUserCountByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .count();
    }


    public Long getTotalUserCount() {
        return userRepository.count();
    }

    public Long getActiveUserCount() {
        return userRepository.countByEnabledTrue();
    }
}