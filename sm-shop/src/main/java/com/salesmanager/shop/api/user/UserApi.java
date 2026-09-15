package com.salesmanager.shop.api.user;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.facade.user.UserFacade;
import com.salesmanager.shop.model.user.PersistableUser;
import com.salesmanager.shop.model.user.ReadableUser;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "APIs quản lý tài khoản Quản trị viên (Admin)")
public class UserApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApi.class);

    private final UserFacade userFacade;

    public UserApi(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    // 1. Tạo mới tài khoản Admin
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = { "/private/user/" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "POST", value = "Creates a new admin user", notes = "", response = ReadableUser.class)
    public ReadableUser create(
            @Valid @RequestBody PersistableUser user, 
            HttpServletRequest request) {
        
        // 1. Kiểm tra trạng thái đăng nhập
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        
        // 2. Kiểm tra quyền hạn: Chỉ SUPERADMIN hoặc ADMIN mới được phép tạo tài khoản
        userFacade.authorizedGroup(authenticatedUser, 
                Stream.of(Constants.GROUP_SUPERADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_RETAIL)
                      .collect(Collectors.toList()));

        // 3. Tiến hành tạo User (đã bỏ tham số merchantStore)
        return userFacade.create(user);
    }

    // 2. Lấy chi tiết Admin theo ID
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin quản trị viên theo ID")
    public ResponseEntity<?> get(@PathVariable Long id) {
        User user = userFacade.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy User với ID: " + id));
        }
        return ResponseEntity.ok(user);
    }

    // 3. Lấy danh sách Admin có phân trang và tìm kiếm theo Email
    @GetMapping
    @Operation(summary = "Lấy danh sách quản trị viên có phân trang")
    public ResponseEntity<Page<User>> list(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "20") Integer count,
            @RequestParam(value = "emailAddress", required = false) String emailAddress) {

        UserCriteria criteria = new UserCriteria();
        if (emailAddress != null && !emailAddress.trim().isEmpty()) {
            criteria.setAdminEmail(emailAddress);
        }

        Page<User> userPage = userFacade.listByCriteria(criteria, page, count);
        return ResponseEntity.ok(userPage);
    }

    // 4. Cập nhật thông tin Admin
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin quản trị viên")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            User updatedUser = userFacade.update(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            LOGGER.error("Lỗi cập nhật User ID: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Đổi mật khẩu Admin
    @PatchMapping("/{id}/password")
    @Operation(summary = "Đổi mật khẩu tài khoản quản trị viên")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("password");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới không được để trống!"));
        }

        try {
            userFacade.changePassword(id, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            LOGGER.error("Lỗi khi đổi mật khẩu cho ID: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Khóa / Kích hoạt tài khoản
    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Bật hoặc tắt kích hoạt tài khoản")
    public ResponseEntity<?> updateEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean active = payload.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trường 'active' là bắt buộc!"));
        }

        User existingUser = userFacade.getById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy User với ID: " + id));
        }

        existingUser.setActive(active);
        userFacade.update(id, existingUser);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái kích hoạt thành công!"));
    }

    // 7. Xóa tài khoản Admin
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tài khoản quản trị viên theo ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            userFacade.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công tài khoản ID: " + id));
        } catch (Exception e) {
            LOGGER.error("Lỗi khi xóa User ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 8. Lấy thông tin tài khoản Admin đang đăng nhập
    @GetMapping("/profile")
    @Operation(summary = "Lấy hồ sơ cá nhân của quản trị viên đang đăng nhập")
    public ResponseEntity<?> getAuthUser(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Chưa đăng nhập!"));
        }

        String userName = principal.getName();
        User user = userFacade.getByUserName(userName);
        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa!"));
        }

        return ResponseEntity.ok(user);
    }
}