package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductRepository productRepository;

    // 1. HIỂN THỊ DANH SÁCH SẢN PHẨM
    @GetMapping
    public String showProductList(Model model) {
        // Lấy tất cả sản phẩm, sắp xếp mới nhất lên đầu
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "admin/products"; // Trả về file templates/admin/products.html
    }

    // 2. MỞ FORM THÊM MỚI
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products-edit"; // Trả về file form (dùng chung cho thêm và sửa)
    }

    // 3. MỞ FORM CHỈNH SỬA
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "admin/products-edit";
    }

    // 4. LƯU SẢN PHẨM (Xử lý cho cả Thêm và Sửa)
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productRepository.save(product);
        return "redirect:/admin/products"; // Lưu xong quay về trang danh sách
    }

    // 5. XÓA SẢN PHẨM
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }
}