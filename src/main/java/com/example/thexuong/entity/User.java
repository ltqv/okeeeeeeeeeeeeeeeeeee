package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "provider_id")
    private String providerId;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    private String provider = "LOCAL"; //'local' hoặc 'google'

    @Builder.Default
    private String role = "USER"; //'user' hoặc 'admin'
}
