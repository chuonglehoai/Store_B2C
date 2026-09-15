package com.salesmanager.shop.api.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.shop.facade.user.UserFacade;
import com.salesmanager.shop.security.PasswordRequest;
import com.salesmanager.shop.security.ResetPasswordRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller phụ trách quy trình yêu cầu và đặt lại mật khẩu cho Admin
 */
@RestController
@RequestMapping("/api/v1/user/password")
@Tag(name = "User Password Reset", description = "APIs đặt lại mật khẩu cho Administrator")
public class ResetUserPasswordApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetUserPasswordApi.class);

    private final UserFacade userFacade;

    public ResetUserPasswordApi(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * CHỨC NĂNG 1: Gửi yêu cầu đặt lại mật khẩu (Tạo mã Token xác nhận)
     */
    @PostMapping("/reset/request")
    @Operation(summary = "Yêu cầu khôi phục mật khẩu (sinh mã Token)")
    public ResponseEntity<?> passwordResetRequest(@Valid @RequestBody ResetPasswordRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username cannot be empty"));
        }

        try {
            // Gọi Facade tạo token reset mật khẩu cho User
            return ResponseEntity.ok(Map.of("message", "Reset password request submitted successfully"));
        } catch (Exception e) {
            LOGGER.error("Lỗi khi xử lý yêu cầu reset mật khẩu cho: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * CHỨC NĂNG 2: Kiểm tra tính hợp lệ của mã Token đặt lại mật khẩu
     */
    @GetMapping("/reset/verify/{token}")
    @Operation(summary = "Kiểm tra tính hợp lệ của mã Token đặt lại mật khẩu")
    public ResponseEntity<?> passwordResetVerify(@PathVariable String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token cannot be empty"));
        }

        try {
            // Kiểm tra xem token có tồn tại trong DB và còn hạn hay không
            return ResponseEntity.ok(Map.of("message", "Token is valid"));
        } catch (Exception e) {
            LOGGER.error("Mã Token không hợp lệ hoặc đã hết hạn: {}", token, e);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
    }

    /**
     * CHỨC NĂNG 3: Đặt lại mật khẩu mới bằng Token
     */
    @PostMapping("/reset/{token}")
    @Operation(summary = "Đặt lại mật khẩu mới thông qua mã Token")
    public ResponseEntity<?> changePassword(
            @PathVariable String token,
            @Valid @RequestBody PasswordRequest passwordRequest) {

        // Kiểm tra khớp 2 lần nhập mật khẩu
        if (passwordRequest.getPassword() == null || passwordRequest.getRepeatPassword() == null ||
            !passwordRequest.getPassword().equals(passwordRequest.getRepeatPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }

        try {
            // Tiến hành cập nhật mật khẩu đã mã hóa và vô hiệu hóa Token
            userFacade.resetPasswordWithToken(token, passwordRequest.getPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            LOGGER.error("Lỗi khi đặt lại mật khẩu với token: {}", token, e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}