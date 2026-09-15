package com.salesmanager.shop.facade.user;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.model.security.ReadableGroup;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.model.user.ReadableUser;

@Service("userFacade")
public class UserFacadeImpl implements UserFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFacadeImpl.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserFacadeImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. TRUY XUẤT THÔNG TIN ---

    @Override
    public ReadableUser getById(Long id) {
        try {
            User user = userService.getById(id);
            return user != null ? convertToReadableUser(user) : null;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi tìm User theo ID: {}", id, e);
            return null;
        }
    }

    @Override
    public ReadableUser getByUserName(String userName) {
        try {
            User user = userService.getByUserName(userName);
            return user != null ? convertToReadableUser(user) : null;
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi tìm User theo username: {}", userName, e);
            return null;
        }
    }

    @Override
    public Page<ReadableUser> listByCriteria(UserCriteria criteria, int page, int count) {
        try {
            Page<User> userPage = userService.listByCriteria(criteria, page, count);
            return userPage.map(this::convertToReadableUser);
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi lấy danh sách User phân trang", e);
            return Page.empty();
        }
    }

    // --- 2. ĐĂNG KÝ / TẠO ADMIN ---

    @Override
    public ReadableUser register(ReadableUser user) throws Exception {
        LOGGER.info("Tạo mới tài khoản Admin: {}", user.getAdminName());

        // Kiểm tra trùng username
        if (userService.getByUserName(user.getAdminName()) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + user.getAdminName());
        }

        // Mã hóa mật khẩu
        if (user.getAdminPassword() != null && !user.getAdminPassword().isEmpty()) {
            user.setAdminPassword(passwordEncoder.encode(user.getAdminPassword()));
        }

        user.setActive(true);
        userService.saveOrUpdate(user);
        return user;
    }

    // --- 3. ĐĂNG NHẬP & XÁC THỰC ---

    @Override
    public boolean authenticate(String userName, String rawPassword) {
        try {
            User user = userService.getByUserName(userName);
            if (user == null || !user.isActive()) {
                return false;
            }
            return passwordEncoder.matches(rawPassword, user.getAdminPassword());
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi xác thực Admin: {}", userName, e);
            return false;
        }
    }

    // --- 4. CẬP NHẬT & XÓA ---

    @Override
    public ReadableUser update(Long id, ReadableUser user) {
        try {
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                throw new IllegalArgumentException("Không tìm thấy User với ID: " + id);
            }

            // Cập nhật các thông tin cơ bản
            existingUser.setAdminEmail(user.getAdminEmail());
            existingUser.setAdminPhone(user.getAdminPhone());
            existingUser.setAvatarUrl(user.getAvatarUrl());
            existingUser.setActive(user.isActive());

            userService.saveOrUpdate(existingUser);
            return existingUser;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi cập nhật User ID: {}", id, e);
            throw new RuntimeException("Cập nhật User thất bại!", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            User user = userService.getById(id);
            if (user != null) {
                userService.delete(user);
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi khi xóa User ID: {}", id, e);
            throw new RuntimeException("Không thể xóa User!", e);
        }
    }

    // --- 5. ĐỔI / ĐẶT LẠI MẬT KHẨU ---

    @Override
    public void changePassword(Long userId, String newPassword) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                throw new IllegalArgumentException("Không tìm thấy User với ID: " + userId);
            }
            user.setAdminPassword(passwordEncoder.encode(newPassword));
            userService.saveOrUpdate(user);
        } catch (Exception e) {
            LOGGER.error("Lỗi đổi mật khẩu cho User ID: {}", userId, e);
            throw new RuntimeException("Đổi mật khẩu thất bại!", e);
        }
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        User user = userService.getByPasswordResetToken(token);
        if (user == null) {
            throw new IllegalArgumentException("Mã token đặt lại mật khẩu không hợp lệ!");
        }
        user.setAdminPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsResetRequest(null); // Xóa token sau khi dùng
        try {
            userService.saveOrUpdate(user);
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi đặt lại mật khẩu bằng token", e);
            throw new RuntimeException("Đặt lại mật khẩu thất bại!", e);
        }
    }
}