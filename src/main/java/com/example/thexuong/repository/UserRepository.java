package com.example.thexuong.repository;

import com.example.thexuong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //tìm user bằng username (dùng cho login thường)
    Optional<User> findByUsername(String username);

    //tìm user bằng email (dùng cho login gg)
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
