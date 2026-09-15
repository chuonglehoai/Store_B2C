package com.salesmanager.shop.security;

import java.io.Serializable;

/**
 * DTO trả về kết quả sau khi xác thực thành công gồm ID tài khoản và Token JWT
 */
public class AuthenticationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String token;

    public AuthenticationResponse() {}

    public AuthenticationResponse(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}