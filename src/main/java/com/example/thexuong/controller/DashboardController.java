package com.example.thexuong.controller;

import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductRepository productRepository;

    @GetMapping
    public String showDashboard(Model model) {
        // SỬA LỖI Ở ĐÂY:
        // Truyền thêm PageRequest.of(0, 5) để lấy Top 5 sản phẩm

        List<Object[]> bestSelling = productRepository.findBestSellingProducts(PageRequest.of(0, 5));

        List<Object[]> slowMoving = productRepository.findSlowMovingProducts(PageRequest.of(0, 5));

        // Gửi dữ liệu sang HTML
        model.addAttribute("bestSelling", bestSelling);
        model.addAttribute("slowMoving", slowMoving);

        return "admin/dashboard";
    }
}