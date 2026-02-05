package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("variantId") Long variantId,
                            @RequestParam(value = "quantity", required = false) Integer quantity,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/products";
        }

        int addQty = (quantity == null || quantity < 1) ? 1 : quantity;

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(currentUser).build()));

        CartItem item = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId)
                .orElseGet(() -> CartItem.builder().cart(cart).productVariant(variant).quantity(0).build());

        int currentQty = item.getQuantity() == null ? 0 : item.getQuantity();
        item.setQuantity(currentQty + addQty);
        cartItemRepository.save(item);

        redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng.");
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);
        List<CartItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (cart != null) {
            items = cartItemRepository.findByCartId(cart.getId());
            for (CartItem item : items) {
                if (item.getProductVariant() != null && item.getProductVariant().getProduct() != null) {
                    BigDecimal price = item.getProductVariant().getProduct().getPrice();
                    if (price != null) {
                        total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/cart/item/{id}/delete")
    public String deleteCartItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cartItemRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam String fullName,
                           @RequestParam String phoneNumber,
                           @RequestParam String address,
                           @RequestParam String paymentMethod,
                           RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);
        if (cart == null) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống.");
            return "redirect:/cart";
        }

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống.");
            return "redirect:/cart";
        }

        if (fullName.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin giao hàng.");
            return "redirect:/cart";
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : items) {
            if (item.getProductVariant() == null || item.getProductVariant().getProduct() == null) {
                continue;
            }
            BigDecimal price = item.getProductVariant().getProduct().getPrice();
            if (price == null) {
                continue;
            }
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);

            details.add(OrderDetail.builder()
                    .productId(item.getProductVariant().getProduct().getId())
                    .productName(item.getProductVariant().getProduct().getName())
                    .price(price)
                    .quantity(item.getQuantity())
                    .totalPrice(lineTotal)
                    .build());
        }

        String normalizedPayment = (paymentMethod == null || paymentMethod.isBlank()) ? "COD" : paymentMethod;
        Order order = Order.builder()
                .user(currentUser)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .address(address)
                .paymentMethod(normalizedPayment)
                .status("PENDING")
                .totalMoney(total)
                .build();

        for (OrderDetail detail : details) {
            detail.setOrder(order);
        }
        order.setOrderDetails(details);

        orderRepository.save(order);
        cartItemRepository.deleteAll(items);

        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công.");
        return "redirect:/my-orders";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(currentUser.getId());
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElse(null);
    }
}
