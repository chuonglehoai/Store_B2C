package com.salesmanager.shop.security.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.Permission;
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
            // 1. Tìm User dưới Database
            User user = userService.getByUserName(username);
            if (user == null) {
                // Ném đúng chuẩn Exception của Spring Security để nó không bị lặp vô hạn
                throw new UsernameNotFoundException("Không tìm thấy Admin: " + username);
            }

            // 2. Chuyển đổi Group và Permission của B2C thành GrantedAuthority của Spring Security
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getGroups() != null) {
                for (Group group : user.getGroups()) {
                    // Thêm tiền tố ROLE_ cho nhóm quyền (VD: ROLE_SUPERADMIN)
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group.getGroupName()));
                    
                    // Thêm các quyền lẻ (nếu có)
                    if (group.getPermissions() != null) {
                        for (Permission permission : group.getPermissions()) {
                            authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
                        }
                    }
                }
            }

            
            return new JWTUser(
                user.getId(),  
                user.getAdminUserName(),               
                user.getAdminName(),          
                user.getAdminEmail(),         
                user.getAdminPassword(),      
                authorities,                 
                user.isActive(),              
                null                         
            );

        } catch (UsernameNotFoundException e) {
            throw e; // Ném thẳng ra ngoài để xử lý 401
        } catch (Exception e) {
            throw new UsernameNotFoundException("Lỗi hệ thống khi tải hồ sơ: " + username, e);
        }
    }
}