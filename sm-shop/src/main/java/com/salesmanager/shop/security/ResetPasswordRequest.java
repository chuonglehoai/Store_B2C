package com.salesmanager.shop.security;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO nhận dữ liệu gửi lên khi người dùng gửi yêu cầu khôi phục mật khẩu quên
 */
public class ResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // Tên đăng nhập hoặc Email tài khoản cần khôi phục mật khẩu
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    // Đường dẫn Callback URL của Frontend (dùng nếu gửi link reset qua Email)
    private String returnUrl;

    public ResetPasswordRequest() {
        super();
    }

    public ResetPasswordRequest(String username) {
        this.username = username;
    }

    public ResetPasswordRequest(String username, String returnUrl) {
        this.username = username;
        this.returnUrl = returnUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}