package com.example.thexuong.config;

import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * 1. UserDetailsService: Tìm kiếm user trong Database để Spring Security kiểm tra.
     * Sử dụng cho cả Login thường (Form) và OAuth2 (nếu cần mapping).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username) // Ưu tiên tìm bằng Email
                .or(() -> userRepository.findByUsername(username)) // Tìm bằng Username nếu không thấy Email
                .map(u -> new org.springframework.security.core.userdetails.User(
                        u.getEmail(), // Dùng Email làm định danh chính trong Session
                        u.getPassword() == null ? "" : u.getPassword(), // Xử lý trường hợp user Google không có pass
                        Collections.singleton(new SimpleGrantedAuthority(u.getRole())) // Lưu ý: Database nên lưu role dạng "ROLE_USER" hoặc "USER" tùy SecurityConfig
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email/username: " + username));
    }

    /**
     * 2. PasswordEncoder: Mã hóa mật khẩu (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 3. AuthenticationProvider: Cung cấp cơ chế xác thực cho Spring Security
     * Kết hợp UserDetailsService (tìm user) và PasswordEncoder (so sánh pass)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 4. AuthenticationManager: Quản lý xác thực
     * Cần thiết nếu bạn muốn thực hiện login thủ công (programmatic login) sau khi đăng ký thành công
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}