package com.example.thexuong.repository;

import com.example.thexuong.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ==========================================
    // 1. CÁC HÀM CHO TRANG BÁN HÀNG (Storefront)
    // ==========================================

    List<Product> findTop4ByOrderByIdDesc();

    Page<Product> findByNameContaining(String keyword, Pageable pageable);

    Page<Product> findBySport(String sport, Pageable pageable);

    Page<Product> findByBrand(String brand, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants v " +
            "LEFT JOIN FETCH v.size " +
            "LEFT JOIN FETCH p.reviews " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") Long id);


    // ==========================================
    // 2. CÁC HÀM CHO ADMIN DASHBOARD (Thống kê)
    // ==========================================

    // Sửa lỗi: Thay "LEFT JOIN Order o ON od.orderId = o.id"
    // Thành "LEFT JOIN od.order o" (Sử dụng quan hệ trực tiếp trong Entity)

    @Query("SELECT p, COALESCE(SUM(od.quantity), 0) as daBan " +
            "FROM Product p " +
            "LEFT JOIN OrderDetail od ON p.id = od.productId " +
            "LEFT JOIN od.order o " +
            "WHERE (o.status = 'SHIPPED' OR o.status = 'COMPLETED' OR o.status IS NULL OR od.id IS NULL) " +
            "GROUP BY p.id, p.name, p.price, p.imageUrl, p.category, p.description, p.brand, p.sport " +
            "ORDER BY daBan DESC")
    List<Object[]> findBestSellingProducts(Pageable pageable);

    @Query("SELECT p, COALESCE(SUM(od.quantity), 0) as daBan " +
            "FROM Product p " +
            "LEFT JOIN OrderDetail od ON p.id = od.productId " +
            "LEFT JOIN od.order o " +
            "WHERE (o.status = 'SHIPPED' OR o.status = 'COMPLETED' OR o.status IS NULL OR od.id IS NULL) " +
            "GROUP BY p.id, p.name, p.price, p.imageUrl, p.category, p.description, p.brand, p.sport " +
            "ORDER BY daBan ASC")
    List<Object[]> findSlowMovingProducts(Pageable pageable);
}