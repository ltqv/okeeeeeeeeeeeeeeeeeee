package com.example.thexuong.security;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// Xử lý sau khi login Google thành công
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Lấy thông tin User từ Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // Có thể lấy thêm tên nếu muốn

        // 2. Đồng bộ User vào Database (Nếu chưa có thì tạo mới)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            return userRepository.save(User.builder()
                    .email(email)
                    .username(email) // Dùng email làm username
                    .password("")    // Google user không cần password
                    .role("USER")
                    .provider("GOOGLE")
                    .build());
        });

        // 3. Thiết lập chuyển hướng về trang chủ
        // Spring Security tự động tạo Session (JSESSIONID cookie), ta không cần làm thủ công.
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}