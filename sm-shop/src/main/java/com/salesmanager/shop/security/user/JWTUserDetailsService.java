package com.salesmanager.shop.security.user;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.user.User;

@Service("jwtAdminDetailsService")
public class JWTUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public JWTUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userService.getByUserName(username);
            if (user == null) {
                throw new UsernameNotFoundException("Không tìm thấy Admin: " + username);
            }

            // Truyền chính xác 9 tham số theo thứ tự của JWTUser constructor
            return new JWTUser(
                user.getId(),                 // 1. Long id
                user.getAdminName(),            // 4. String lastname
                user.getAdminEmail(),         // 5. String email
                user.getAdminPassword(),      // 6. String password
                Collections.emptyList(),      // 7. Collection<? extends GrantedAuthority> authorities
                user.isActive(),              // 8. boolean enabled
                null                          // 9. Date lastPasswordResetDate
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Lỗi truy vấn người dùng: " + username, e);
        }
    }
}