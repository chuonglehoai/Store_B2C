package com.salesmanager.shop.facade.user;

import java.util.List;

import org.springframework.data.domain.Page;

import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.model.user.PersistableUser;
import com.salesmanager.shop.model.user.ReadableUser;
import com.salesmanager.shop.model.security.ReadableGroup;

public interface UserFacade {

    // 1. Truy xuất thông tin
    ReadableUser getById(Long id);

    ReadableUser getByUserName(String userName);
    
    List<ReadablePermission> findPermissionsByGroups(List<Integer> ids);

    List<ReadableGroup>listAvailableGroups();

    Page<ReadableUser> listByCriteria(UserCriteria criteria, int page, int count);


    // 2. Tạo tài khoản Quản trị viên
    ReadableUser create(PersistableUser user) throws Exception;

    // 3. Đăng nhập / Xác thực
    boolean authenticate(String userName, String rawPassword);

    // 4. Cập nhật & Xóa
    ReadableUser update(Long id, PersistableUser user);
    void changeStatus(Long id, boolean active);

    void delete(Long id);
    

    // 5. Đổi / Đặt lại mật khẩu
    void changePassword(Long userId, String newPassword);
    void requestPasswordReset(String email) throws Exception;

    void resetPasswordWithToken(String token, String newPassword) throws Exception;
}
