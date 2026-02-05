package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Controller
public class ProductController {
    @Autowired
    private final ProductRepository productRepository;

    // Trang chủ: Chỉ load 4 sản phẩm mới nhất
    @GetMapping(value = {"/", "/index"})
    public String home(Model model) {
        List<Product> newProducts = productRepository.findTop4ByOrderByIdDesc();
        model.addAttribute("products", newProducts);
        return "index";
    }

    // Trang danh sách tất cả sản phẩm
    @GetMapping("/products")
    public String showProductList(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String sport,
                                  @RequestParam(required = false) String brand,
                                  @RequestParam(required = false, defaultValue = "newest") String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int size,
                                  Model model) {
        // 1. Xác định kiểu sắp xếp
        Sort sorting = Sort.by("id").descending(); // Mặc định là mới nhất
        if ("price_asc".equals(sort)) {
            sorting = Sort.by("price").ascending();
        } else if ("price_desc".equals(sort)) {
            sorting = Sort.by("price").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> productsPage;
        if (keyword != null && !keyword.isEmpty()) {
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else if (sport != null && !sport.isEmpty()) {
            productsPage = productRepository.findBySport(sport, pageable);
        } else if (brand != null && !brand.isEmpty()) {
            productsPage = productRepository.findByBrand(brand, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        // 3. Truyền dữ liệu ra View
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("sort", sort); // Để giữ trạng thái dropdown
        model.addAttribute("keyword", keyword); // Để giữ từ khóa tìm kiếm
        model.addAttribute("sport", sport);
        model.addAttribute("brand", brand);

        return "products";
    }

    // Chi tiết sản phẩm
    @GetMapping("/product-detail/{id}")
    public String showProductDetail(@PathVariable Long id,
                                    @RequestParam(required = false) String size,
                                    Model model) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        List<ProductVariant> variants = product.getVariants();
        List<String> allSizes = new ArrayList<>();

        if (variants != null && !variants.isEmpty()) {
            allSizes = variants.stream()
                    .filter(v -> v != null && v.getSize() != null && v.getSize().getName() != null)
                    .map(v -> v.getSize().getName())
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        }

        int quantity = 0;
        Long selectedVariantId = null;

        if (size != null && !size.isBlank() && variants != null && !variants.isEmpty()) {
            ProductVariant variant = variants.stream()
                    .filter(v -> v != null && v.getSize() != null && v.getSize().getName() != null)
                    .filter(v -> size.equals(v.getSize().getName()))
                    .findFirst()
                    .orElse(null);

            if (variant != null) {
                quantity = variant.getQuantity();
                selectedVariantId = variant.getId();
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("sizes", allSizes);
        model.addAttribute("selectedSize", size);
        model.addAttribute("quantity", quantity);
        model.addAttribute("selectedVariantId", selectedVariantId);

        return "product-detail";
    }
}