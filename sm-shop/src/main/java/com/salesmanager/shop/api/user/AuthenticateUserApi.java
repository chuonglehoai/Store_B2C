package com.salesmanager.shop.api.user;

import java.util.Map;
import java.util.Date;

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
import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.user.User;

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
    private final UserService userService;

    // Sử dụng Constructor Injection thay thế @Inject / @Autowired cũ
    public AuthenticateUserApi(
            AuthenticationManager jwtAdminAuthenticationManager,
            UserDetailsService jwtAdminDetailsService,
            JWTTokenUtil jwtTokenUtil,
            UserService userService) {
        this.jwtAdminAuthenticationManager = jwtAdminAuthenticationManager;
        this.jwtAdminDetailsService = jwtAdminDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    /**
     * CHỨC NĂNG 1: Đăng nhập quản trị viên bằng username & password
     * Trả về JWT Token và ID người dùng khi thông tin đăng nhập hợp lệ
     */
    @PostMapping("/private/login")
    @Operation(summary = "Đăng nhập tài khoản quản trị viên")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        try {
            User userModel = userService.getByUserName(authenticationRequest.getUsername());
            if (userModel == null) {
                throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
            }
            // 2. Kiểm tra KHÓA VĨNH VIỄN (active = 0)
            if (!userModel.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Tài khoản đã bị khóa bảo mật. Vui lòng liên hệ SuperAdmin."));
            }

            // 3. Kiểm tra KHÓA TẠM THỜI (15 phút)
            int currentFails = userModel.getFailedLoginAttempts() == null ? 0 : userModel.getFailedLoginAttempts();
            if (currentFails >= 5 && userModel.getLockTime() != null) {
                long lockDurationMillis = System.currentTimeMillis() - userModel.getLockTime().getTime();
                long fifteenMinutesMillis = 15 * 60 * 1000;
                
                if (lockDurationMillis < fifteenMinutesMillis) {
                    long minutesLeft = (fifteenMinutesMillis - lockDurationMillis) / 60000;
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(Map.of("error", "Tài khoản bị khóa tạm thời. Vui lòng thử lại sau " + minutesLeft + " phút."));
                }
            }

            // 4. Bắt đầu xác thực qua AuthenticationManager
            Authentication authentication;
            try {
                authentication = jwtAdminAuthenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authenticationRequest.getUsername(),
                                authenticationRequest.getPassword()
                        )
                );
            } catch (BadCredentialsException e) {
                // XỬ LÝ KHI NHẬP SAI MẬT KHẨU
                currentFails++;
                userModel.setFailedLoginAttempts(currentFails);

                if (currentFails == 5) {
                    userModel.setLockTime(new java.util.Date());
                    userService.update(userModel); 
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(Map.of("error", "Nhập sai mật khẩu 5 lần. Tài khoản bị khóa tạm thời 15 phút."));
                } else if (currentFails > 5) {
                    userModel.setActive(false);
                    userService.update(userModel);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Tài khoản đã bị khóa vĩnh viễn do nhập sai quá nhiều lần."));
                }
                userService.update(userModel);
                throw e;
            }

            userModel.setFailedLoginAttempts(0);
            userModel.setLockTime(null);
            userModel.setLastLogin(new Date()); 
            userService.update(userModel);
            

            SecurityContextHolder.getContext().setAuthentication(authentication);

            //  Tải thông tin UserDetails và sinh chuỗi Token
            JWTUser userDetails = (JWTUser) jwtAdminDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);
            
            return ResponseEntity.ok(new AuthenticationResponse(userDetails.getId(), token));

        } catch (BadCredentialsException e) {
            // Bắt lỗi sai mật khẩu hoặc không tìm thấy username
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu!"));
        } catch (org.springframework.security.authentication.DisabledException e) {
            // Bắt lỗi tài khoản có active = false
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt!"));
        } catch (Exception e) {
            // Bắt các lỗi sập hệ thống do code hoặc Database
            LOGGER.error("Lỗi sập hệ thống khi xác thực user: {}", authenticationRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống trong quá trình đăng nhập: " + e.getMessage()));
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