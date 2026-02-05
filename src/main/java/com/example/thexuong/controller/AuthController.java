package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Trả về file login.html
    }

    // 2. Hiển thị trang đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User()); // Tạo object rỗng để form binding
        return "register"; // Trả về file register.html
    }

    // 3. Xử lý logic đăng ký (Form Submit)
    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, Model model) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email đã tồn tại!");
            return "register";
        }

        try {
            // Thiết lập thông tin mặc định
            user.setUsername(user.getEmail()); // Dùng email làm username
            user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu
            user.setRole("USER");      // Gán quyền mặc định
            user.setProvider("LOCAL"); // Gán provider là đăng ký tại web

            userRepository.save(user);

            // Đăng ký thành công -> Chuyển hướng về trang login kèm thông báo
            return "redirect:/login?registerSuccess";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            return "register";
        }
    }
}