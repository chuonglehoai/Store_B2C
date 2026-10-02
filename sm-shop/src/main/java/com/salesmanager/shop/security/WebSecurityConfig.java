package com.salesmanager.shop.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.salesmanager.shop.api.user.JWTAdminAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthenticationTokenFilter authenticationTokenFilter;

    public WebSecurityConfig(AuthenticationTokenFilter authenticationTokenFilter) {
        this.authenticationTokenFilter = authenticationTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JWTAdminAuthenticationProvider authenticationProvider(
            @Qualifier("jwtAdminDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        return new JWTAdminAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF
            .csrf(csrf -> csrf.disable())

            // 2. Không lưu session (chuẩn JWT Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Phân quyền Endpoint
            .authorizeHttpRequests(auth -> auth
                // Cho phép Swagger UI & OpenAPI Docs
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error",
                    "/uploads/**"
                ).permitAll()

                // CÁC ĐƯỜNG DẪN CÔNG KHAI DÀNH CHO CUSTOMER & ADMIN
                .requestMatchers(
                    // Luồng Customer
                    "/api/v1/customer/register", // Dữ phòng API cũ
                    "/api/v1/customer/login",    // Dữ phòng API cũ
                    "/api/v1/customers/register",
                    "/api/v1/customers/login",
                    "/api/v1/customers/password/**",
                    
                    // Luồng Admin
                    "/api/v1/private/login",         // Admin Đăng nhập
                    "/api/v1/auth/**",               // Refresh Token
                    "/api/v1/user/password/**",

                    // Luồng Sản phẩm & Danh mục
                    "/api/v1/products/import",
                    "/api/v1/products/**",
                    "/api/v1/categories/**"
                ).permitAll()

                // Các API quản trị/cá nhân còn lại bắt buộc có Token (Bao gồm cả Tạo Admin và Lấy thông tin ID)
                .anyRequest().authenticated()
            )

            // 4. Thêm filter kiểm tra JWT trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}