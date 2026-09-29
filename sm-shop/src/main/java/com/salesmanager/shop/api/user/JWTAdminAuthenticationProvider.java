package com.salesmanager.shop.api.user;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Custom authautentication provider for admin api
 * @author carlsamson
 *
 */
public class JWTAdminAuthenticationProvider extends DaoAuthenticationProvider {
	
    private final UserDetailsService jwtAdminDetailsService;
    private final PasswordEncoder passwordEncoder;

    // Sử dụng Constructor để tiêm dependency thay vì @Autowired
    public JWTAdminAuthenticationProvider(UserDetailsService jwtAdminDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtAdminDetailsService = jwtAdminDetailsService;
        this.passwordEncoder = passwordEncoder;
        
        // Bắt buộc set 2 trường này cho lớp cha DaoAuthenticationProvider
        this.setUserDetailsService(jwtAdminDetailsService);
        this.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        // 1. Tải thông tin User từ Database
        UserDetails user = jwtAdminDetailsService.loadUserByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu!");
        }

        // 2. So sánh mật khẩu gốc với mật khẩu BĂM TRONG DB (Đã sửa lỗi logic)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu!");
        }

        // 3. Kiểm tra trạng thái tài khoản (Tránh user bị khóa vẫn đăng nhập được)
        if (!user.isEnabled()) {
            throw new DisabledException("Tài khoản của bạn đã bị khóa!");
        }

        // 4. Trả về Token xác thực thành công kèm theo danh sách Quyền (Authorities)
        return new UsernamePasswordAuthenticationToken(user, rawPassword, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
