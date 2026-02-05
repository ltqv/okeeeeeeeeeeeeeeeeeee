package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "orderDetails"})
    List<Order> findAllByOrderByIdDesc();
}
