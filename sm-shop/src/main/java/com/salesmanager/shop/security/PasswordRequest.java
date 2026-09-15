package com.salesmanager.shop.security;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO nhận dữ liệu gửi lên khi thực hiện đổi mật khẩu hoặc đặt lại mật khẩu mới
 * Kế thừa trường 'password' (mật khẩu mới) từ AuthenticationRequest
 */
public class PasswordRequest extends AuthenticationRequest {

    private static final long serialVersionUID = 1L;

    // Mật khẩu hiện tại (dùng trong nghiệp vụ đổi mật khẩu khi đã đăng nhập)
    private String current;

    // Mật khẩu xác nhận lại (phải trùng khớp với trường password)
    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    private String repeatPassword;

    public PasswordRequest() {
        super();
    }

    public PasswordRequest(String username, String newPassword, String current, String repeatPassword) {
        super(username, newPassword);
        this.current = current;
        this.repeatPassword = repeatPassword;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}