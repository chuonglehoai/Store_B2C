package com.salesmanager.shop.security;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.salesmanager.core.model.common.UserContext;
import com.salesmanager.shop.security.common.CustomAuthenticationManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter chặn mọi request gửi lên để kiểm tra Header Authorization chứa JWT Token
 */
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFilter.class);
    private static final String BEARER_TOKEN = "Bearer ";

    @Value("${authToken.header:Authorization}")
    private String tokenHeader;

    private final CustomAuthenticationManager jwtCustomCustomerAuthenticationManager;
    private final CustomAuthenticationManager jwtCustomAdminAuthenticationManager;

    public AuthenticationTokenFilter(
            CustomAuthenticationManager jwtCustomCustomerAuthenticationManager,
            CustomAuthenticationManager jwtCustomAdminAuthenticationManager) {
        this.jwtCustomCustomerAuthenticationManager = jwtCustomCustomerAuthenticationManager;
        this.jwtCustomAdminAuthenticationManager = jwtCustomAdminAuthenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestUrl = request.getRequestURI();

        // 1. BYPASS NGAY CÁC ENDPOINT CÔNG KHAI (Không cần kiểm tra token, không cần set UserContext)
        if (requestUrl.contains("/customers/register")
                || requestUrl.contains("/customers/login")
                || requestUrl.contains("/customer/register")
                || requestUrl.contains("/customer/login")
                || requestUrl.contains("/swagger")
                || requestUrl.contains("/v3/api-docs")
                || requestUrl.contains("/error")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Ghi nhận IP người dùng vào Context cho các request còn lại
        try {
            String ipAddress = request.getRemoteAddr();
            UserContext userContext = UserContext.create();
            userContext.setIpAddress(ipAddress);
        } catch (Exception e) {
            LOGGER.error("Lỗi khi lưu IP Address vào UserContext", e);
        }

        String requestHeader = request.getHeader(this.tokenHeader);

        // 3. Kiểm tra và xác thực JWT
        try {
            if (StringUtils.isNotBlank(requestHeader) && requestHeader.startsWith(BEARER_TOKEN)) {
                
                // Route dành cho Customer (/api/v1/customers/** hoặc /api/v1/auth/**)
                if (requestUrl.contains("/api/v1/customers") || requestUrl.contains("/api/v1/auth")) {
                    jwtCustomCustomerAuthenticationManager.authenticateRequest(request, response);
                } 
                // Route dành cho Admin (/api/v1/private/** hoặc /api/v1/users/**)
                else if (requestUrl.contains("/api/v1/private") || requestUrl.contains("/api/v1/users")) {
                    jwtCustomAdminAuthenticationManager.authenticateRequest(request, response);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi xác thực Token cho request [{}]: {}", requestUrl, e.getMessage());
        }

        // 4. Cho phép request đi tiếp qua chuỗi Filter
        try {
            chain.doFilter(request, response);
        } finally {
            // Dọn dẹp UserContext tránh memory leak
            postFilter();
        }
    }

    /**
     * Dọn dẹp ThreadLocal Context sau khi hoàn tất request
     */
    private void postFilter() {
        try {
            UserContext userContext = UserContext.getCurrentInstance();
            if (userContext != null) {
                userContext.close();
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi giải phóng UserContext", e);
        }
    }
}