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

    // 생성자 주입으로 순환 참조 해결
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // UserDetailsService implementation for Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ✅ 추가: username으로 User 엔티티 직접 가져오기 (PlaylistService에서 필요)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ✅ 추가: User 저장 메서드 (DataInitializer에서 필요)
    public User saveUser(User user) {
        // 비밀번호가 암호화되지 않았다면 암호화
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // Create new user
    public void createUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        // Check if email already exists
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    // Update user
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    // Check if new username is taken by another user
                    if (!user.getUsername().equals(userDetails.getUsername()) &&
                            userRepository.existsByUsername(userDetails.getUsername())) {
                        throw new RuntimeException("Username already exists: " + userDetails.getUsername());
                    }

                    // Check if new email is taken by another user
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

                    // Only update password if it's provided and not empty
                    if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Change password
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Enable/Disable user
    public void toggleUserStatus(Long id) {
        userRepository.findById(id)
                .ifPresent(user -> {
                    user.setEnabled(!user.isEnabled());
                    userRepository.save(user);
                });
    }

    // Search users
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(searchTerm.trim());
    }

    // Get users by role
    public List<User> getUsersByRole(String role) {
        // Role을 String으로 받아서 처리
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .toList();
    }

    // Get enabled users
    public List<User> getEnabledUsers() {
        return userRepository.findByEnabledTrue();
    }

    // Get disabled users
    public List<User> getDisabledUsers() {
        return userRepository.findByEnabledFalse();
    }

    // Check if username exists
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Get user count by role
    public Long getUserCountByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .count();
    }

    // Get total user count
    public Long getTotalUserCount() {
        return userRepository.count();
    }

    // Get active user count
    public Long getActiveUserCount() {
        return userRepository.countByEnabledTrue();
    }
}