package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String showUsers(@RequestParam(required = false) Long editId, Model model) {
        List<User> users = userRepository.findAll(Sort.by("id").ascending());
        User formUser = new User();
        boolean isEdit = false;

        if (editId != null) {
            Optional<User> editUser = userRepository.findById(editId);
            if (editUser.isPresent()) {
                User source = editUser.get();
                formUser.setId(source.getId());
                formUser.setEmail(source.getEmail());
                formUser.setUsername(source.getUsername());
                formUser.setFullName(source.getFullName());
                formUser.setRole(source.getRole());
                formUser.setProvider(source.getProvider());
                isEdit = true;
            }
        }

        model.addAttribute("users", users);
        model.addAttribute("formUser", formUser);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("currentUserId", getCurrentUserId());

        return "admin/users";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("formUser") User formUser,
                           @RequestParam(value = "admin", required = false) String admin,
                           RedirectAttributes redirectAttributes) {
        boolean isAdmin = admin != null;
        String role = isAdmin ? "ADMIN" : "USER";

        if (formUser.getId() == null) {
            if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Email không được để trống.");
                return "redirect:/admin/users";
            }
            if (userRepository.existsByEmail(formUser.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại.");
                return "redirect:/admin/users";
            }
            if (formUser.getUsername() != null && !formUser.getUsername().isBlank()) {
                if (userRepository.findByUsername(formUser.getUsername()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Username đã tồn tại.");
                    return "redirect:/admin/users";
                }
            } else {
                formUser.setUsername(formUser.getEmail());
            }
            if (formUser.getPassword() == null || formUser.getPassword().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống.");
                return "redirect:/admin/users";
            }

            formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
            formUser.setRole(role);
            if (formUser.getProvider() == null || formUser.getProvider().isBlank()) {
                formUser.setProvider("LOCAL");
            }
            userRepository.save(formUser);
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công.");
            return "redirect:/admin/users";
        }

        User existing = userRepository.findById(formUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email không được để trống.");
            return "redirect:/admin/users";
        }

        Optional<User> emailOwner = userRepository.findByEmail(formUser.getEmail());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(existing.getId())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại.");
            return "redirect:/admin/users";
        }

        if (formUser.getUsername() != null && !formUser.getUsername().isBlank()) {
            Optional<User> usernameOwner = userRepository.findByUsername(formUser.getUsername());
            if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(existing.getId())) {
                redirectAttributes.addFlashAttribute("error", "Username đã tồn tại.");
                return "redirect:/admin/users";
            }
            existing.setUsername(formUser.getUsername());
        } else {
            existing.setUsername(formUser.getEmail());
        }

        existing.setEmail(formUser.getEmail());
        existing.setFullName(formUser.getFullName());
        existing.setRole(role);

        if (formUser.getPassword() != null && !formUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(formUser.getPassword()));
        }

        userRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể tự xóa tài khoản của mình.");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng.");
        return "redirect:/admin/users";
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .map(User::getId)
                .orElse(null);
    }
}
