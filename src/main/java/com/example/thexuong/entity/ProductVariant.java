package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "[ProductVariants]")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @Builder.Default
    private Integer quantity = 0;

    @Column(length = 50)
    private String sku;

}
