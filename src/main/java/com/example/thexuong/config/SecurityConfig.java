package com.example.thexuong.config;

import com.example.thexuong.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationProvider authenticationProvider; // 1. Inject từ ApplicationConfig

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để đơn giản hóa việc submit form (nếu bật, cần thêm input hidden csrf trong form html)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập public các trang này
                        .requestMatchers("/", "/index", "/login", "/register", "/products/**", "/product-detail/**").permitAll()
                        // Cho phép truy cập resources
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/fonts/**", "/uploads/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login") // Phải trùng với th:action trong form login
                        .defaultSuccessUrl("/", true)      // True: Luôn về trang chủ sau khi login
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")        // Quan trọng: Form gửi lên name="email"
                        .passwordParameter("password")
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)       // Hủy session cũ
                        .deleteCookies("JSESSIONID")       // Xóa cookie
                        .permitAll()
                );

        return http.build();
    }
}
