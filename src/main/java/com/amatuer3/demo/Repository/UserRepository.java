package com.amatuer3.demo.Repository;

import com.amatuer3.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Ongeza hii
    Optional<User> findByResetToken(String resetToken); // Ongeza hii
}