package com.salesmanager.shop.facade.user;

import org.springframework.data.domain.Page;

import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.model.user.ReadableUser;

public interface UserFacade {

    // 1. Truy xuất thông tin
    ReadableUser getById(Long id);

    ReadableUser getByUserName(String userName);

    Page<ReadableUser> listByCriteria(UserCriteria criteria, int page, int count);

    // 2. Tạo tài khoản Quản trị viên
    ReadableUser Create(ReadableUser user) throws Exception;

    // 3. Đăng nhập / Xác thực
    boolean authenticate(String userName, String rawPassword);

    // 4. Cập nhật & Xóa
    ReadableUser update(Long id, ReadableUser user);

    void delete(Long id);

    // 5. Đổi / Đặt lại mật khẩu
    void changePassword(Long userId, String newPassword);

    void resetPasswordWithToken(String token, String newPassword);
}
