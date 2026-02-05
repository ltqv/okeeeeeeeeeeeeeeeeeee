package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "OrderDetails")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    private Long productId; // Chỉ lưu ID để tham chiếu, không join cứng để tránh lỗi khi xóa SP

    @Column(name = "product_name")
    private String productName;

    @Transient
    private String size;

    private BigDecimal price;

    private Integer quantity;

    @Column(name = "total_price")
    private BigDecimal totalPrice;
}
