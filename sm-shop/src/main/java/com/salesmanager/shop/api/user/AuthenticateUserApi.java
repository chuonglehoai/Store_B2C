package com.salesmanager.shop.api.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.shop.security.AuthenticationRequest;
import com.salesmanager.shop.security.AuthenticationResponse;
import com.salesmanager.shop.security.JWTTokenUtil;
import com.salesmanager.shop.security.user.JWTUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller phụ trách xác thực và cấp phát mã JWT cho tài khoản Quản trị viên (Admin)
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "User Authentication", description = "APIs đăng nhập và cấp quyền cho Administrator")
public class AuthenticateUserApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateUserApi.class);

    @Value("${authToken.header:Authorization}")
    private String tokenHeader;

    private final AuthenticationManager jwtAdminAuthenticationManager;
    private final UserDetailsService jwtAdminDetailsService;
    private final JWTTokenUtil jwtTokenUtil;

    // Sử dụng Constructor Injection thay thế @Inject / @Autowired cũ
    public AuthenticateUserApi(
            AuthenticationManager jwtAdminAuthenticationManager,
            UserDetailsService jwtAdminDetailsService,
            JWTTokenUtil jwtTokenUtil) {
        this.jwtAdminAuthenticationManager = jwtAdminAuthenticationManager;
        this.jwtAdminDetailsService = jwtAdminDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * CHỨC NĂNG 1: Đăng nhập quản trị viên bằng username & password
     * Trả về JWT Token và ID người dùng khi thông tin đăng nhập hợp lệ
     */
    @PostMapping("/private/login")
    @Operation(summary = "Đăng nhập tài khoản quản trị viên")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        try {
            // Xác thực thông tin đăng nhập qua Spring Security AuthenticationManager
            Authentication authentication = jwtAdminAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            // Thiết lập phiên xác thực vào Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tải chi tiết người dùng và khởi tạo JWT Token
            JWTUser userDetails = (JWTUser) jwtAdminDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponse(userDetails.getId(), token));

        } catch (BadCredentialsException e) {
            // Trả về lỗi 401 nếu sai tài khoản hoặc mật khẩu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Bad credentials"));
        } catch (Exception e) {
            LOGGER.error("Lỗi trong quá trình xác thực user: {}", authenticationRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Authentication error"));
        }
    }

    /**
     * CHỨC NĂNG 2: Làm mới JWT Token (Refresh Token)
     * Đọc token hiện tại từ Request Header và gia hạn thời gian sống nếu còn hợp lệ
     */
    @GetMapping("/auth/refresh")
    @Operation(summary = "Làm mới mã JWT Token")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);

        // Chuẩn hóa token: bỏ tiền tố "Bearer " nếu có
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token header is missing"));
        }

        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            JWTUser user = (JWTUser) jwtAdminDetailsService.loadUserByUsername(username);

            // Kiểm tra tính hợp lệ và thời gian ân hạn của Token
            if (jwtTokenUtil.canTokenBeRefreshedWithGrace(token, user.getLastPasswordResetDate())) {
                String refreshedToken = jwtTokenUtil.refreshToken(token);
                return ResponseEntity.ok(new AuthenticationResponse(user.getId(), refreshedToken));
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi làm mới JWT token", e);
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Token cannot be refreshed or has expired"));
    }
}