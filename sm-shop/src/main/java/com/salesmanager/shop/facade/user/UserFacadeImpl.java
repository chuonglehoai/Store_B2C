package com.salesmanager.shop.facade.user;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.business.services.email.EmailService;
import com.salesmanager.core.business.services.group.GroupService;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.Permission;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.model.security.ReadableGroup;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.model.user.PersistableUser;
import com.salesmanager.shop.model.user.ReadableUser;


import com.salesmanager.shop.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.api.exception.ServiceRuntimeException;

@Service("userFacade")
public class UserFacadeImpl implements UserFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFacadeImpl.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PermissionService permissionService;
    private final GroupService groupService;

    public UserFacadeImpl(UserService userService, PasswordEncoder passwordEncoder, EmailService emailService, PermissionService permissionService, GroupService groupService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.permissionService = permissionService;
        this.groupService = groupService;
    }

    // --- 1. TRUY XUẤT THÔNG TIN ---

    @Override
    public ReadableUser getById(Long id) {
        try {
            User user = userService.getById(id);
            return user != null ? convertUserToReadableUser(user) : null;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi tìm User theo ID: {}", id, e);
            return null;
        }
    }

    @Override
    public ReadableUser getByUserName(String userName) {
        try {
            User user = userService.getByUserName(userName);
            return user != null ? convertUserToReadableUser(user) : null;
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi tìm User theo tên đăng nhập: {}", userName, e);
            return null;
        }
    }

    @Override
    public Page<ReadableUser> listByCriteria(UserCriteria criteria, int page, int count) {
        try {
            Page<User> userPage = userService.listByCriteria(criteria, page, count);
            return userPage.map(this::convertUserToReadableUser);
        } catch (ServiceException e) {
            LOGGER.error("Lỗi khi lấy danh sách User phân trang", e);
            return Page.empty();
        }
    }

    @Override
    public ReadableUser create(PersistableUser persistableUser) throws Exception {
        LOGGER.info("Tạo mới tài khoản Admin: {}", persistableUser.getEmailAddress());

        // 1. Kiểm tra trùng email
        if (userService.getByUserName(persistableUser.getAdminUserName()) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + persistableUser.getAdminUserName());
        }

        // 2. Chuyển đổi từ DTO (Form input) sang Entity (Database model)
        User userEntity = new User();
        userEntity = convertPersistableUserToUser(userEntity, persistableUser);

        // 3. Mã hóa mật khẩu (lấy từ PersistableUser)
        String rawPassword = persistableUser.getPassword();
        if (rawPassword != null && !rawPassword.isEmpty()) {
            userEntity.setAdminPassword(passwordEncoder.encode(rawPassword));
        }

        if (persistableUser.getGroups() == null || persistableUser.getGroups().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản phải được gán ít nhất 1 nhóm quyền (Group)!");
        }

        Set<Group> userGroups = persistableUser.getGroups().stream().map(dtoGroup -> {
            Group group = new Group();
            group.setId(dtoGroup.getId().intValue()); 
            return group;
        }).collect(Collectors.toSet());
        userEntity.setGroups(userGroups);
        userEntity.setActive(true);

        // 4. Lưu xuống Database (Lưu ý: chưa có logic xử lý Group/Permission ở đây)
        userService.saveOrUpdate(userEntity);

        // 5. Chuyển đổi Entity thành ReadableUser để trả về cho Client hiển thị
        return convertUserToReadableUser(userEntity);
    }

    /*ReadableGroup
    id,name,type */
    private ReadableUser convertUserToReadableUser(User user) {
        if (user == null) {
            return null;
        }

        try {
            ReadableUser readableUser = new ReadableUser();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            // Map các thông tin cơ bản từ Entity sang Readable DTO
            readableUser.setId(user.getId());
            readableUser.setAdminName(user.getAdminName());
            readableUser.setEmailAddress(user.getAdminEmail());
            readableUser.setAdminPhone(user.getAdminPhone());
            readableUser.setAdminAddress(user.getAdminAddress());
            readableUser.setAvatarUrl(user.getAvatarUrl());
            readableUser.setActive(user.isActive());
            readableUser.setAdminUserName(user.getAdminUserName());

            // Xử lý map danh sách nhóm quyền (Groups) nếu User có liên kết với Groups
            if (user.getGroups() != null) {
                List<ReadableGroup> readableGroups = user.getGroups().stream().map(group -> {
                    ReadableGroup rg = new ReadableGroup();
                    rg.setId(group.getId().longValue());
                    rg.setName(group.getGroupName());
                    return rg;
                }).collect(Collectors.toList());
                readableUser.setGroups(readableGroups);

                // Trích xuất ID các nhóm quyền để tìm danh sách Permissions chi tiết
                List<Integer> groupIds = user.getGroups().stream()
                        .map(group -> group.getId())
                        .collect(Collectors.toList());
                
                List<ReadablePermission> permissions = findPermissionsByGroups(groupIds);
                readableUser.setPermissions(permissions);
            }

            if (user.getAuditSection() != null && user.getAuditSection().getDateCreated() != null) {
                readableUser.setDateCreated(dateFormat.format(user.getAuditSection().getDateCreated()));
            }

            if (user.getAuditSection() != null && user.getAuditSection().getDateModified() != null) {
                readableUser.setDateModified(dateFormat.format(user.getAuditSection().getDateModified()));
            }

            if (user.getLastLogin() != null) {
                readableUser.setLastLogin(dateFormat.format(user.getLastLogin()));
            }


            return readableUser;
        } catch (Exception e) {
            throw new ConversionRuntimeException(e);
        }
    }

    private User convertPersistableUserToUser(User userModel, PersistableUser persistableUser) {
        try {
            // Map trực tiếp các trường cơ bản từ DTO vào Entity
            userModel.setAdminEmail(persistableUser.getEmailAddress());
            userModel.setAdminPhone(persistableUser.getAdminPhone());
            userModel.setAdminAddress(persistableUser.getAdminAddress());
            userModel.setAvatarUrl(persistableUser.getAvatarUrl());
            userModel.setAdminName(persistableUser.getAdminName());
            userModel.setAdminUserName(persistableUser.getEmailAddress());

            return userModel;
        } catch (Exception e) {
            throw new ConversionRuntimeException(e);
        }
    }

    /*ReadablePermission
    id,name */

    @Override
	public List<ReadablePermission> findPermissionsByGroups(List<Integer> ids) {
		return getPermissionsByIds(ids).stream().map(permission -> convertPermissionToReadablePermission(permission))
				.collect(Collectors.toList());
	}
    
    private ReadablePermission convertPermissionToReadablePermission(Permission permission) {
		ReadablePermission readablePermission = new ReadablePermission();
		readablePermission.setId(permission.getId());
		readablePermission.setName(permission.getPermissionName());
		return readablePermission;
	}
    
    private List<Permission> getPermissionsByIds(List<Integer> ids) {
		try {
			return permissionService.getPermissions(ids);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

    // ---  ĐĂNG NHẬP & XÁC THỰC ---

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
    public ReadableUser update(Long id, PersistableUser user) {
        try {
            // 1. Lấy thông tin User cũ từ Database (đã có sẵn ID và mật khẩu cũ)
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                throw new IllegalArgumentException("Không tìm thấy User với ID: " + id);
            }

            existingUser = convertPersistableUserToUser(existingUser, user);

            userService.saveOrUpdate(existingUser);

            // 4. Chuyển đổi về DTO thân thiện để trả về cho Client
            return convertUserToReadableUser(existingUser);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi cập nhật User ID: {}", id, e);
            throw new RuntimeException("Cập nhật User thất bại!", e);
        }
    }

    @Override
    public void changeStatus(Long id, boolean active) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                throw new IllegalArgumentException("Không tìm thấy User với ID: " + id);
            }
            user.setActive(active);
            userService.saveOrUpdate(user); // Lưu trạng thái mới xuống DB
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi thay đổi trạng thái User ID: {}", id, e);
            throw new RuntimeException("Cập nhật trạng thái thất bại!", e);
        }
    }

    @Override
    public List<ReadableGroup> listAvailableGroups() {
        try {
            return groupService.list().stream()
                .filter(group -> {
                    // 1. Loại bỏ nhóm CUSTOMER (dựa theo type)
                    boolean isNotCustomer = group.getGroupType() == null || !group.getGroupType().name().equals("CUSTOMER");
                    // 2. Loại bỏ nhóm SUPERADMIN (dựa theo tên)
                    boolean isNotSuperAdmin = !group.getGroupName().equals("SUPERADMIN");
                    
                    return isNotCustomer && isNotSuperAdmin;
                })
                .map(group -> {
                    ReadableGroup rg = new ReadableGroup();
                    rg.setId(group.getId().longValue());
                    rg.setName(group.getGroupName());
                    return rg;
                }).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Lỗi khi lấy danh sách Group", e);
            return java.util.Collections.emptyList();
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
    public void requestPasswordReset(String email) throws Exception{
        // 1. Tìm Admin theo email
        User userModel = userService.getByUserName(email); 
        
        if (userModel == null) {
            LOGGER.warn("Yêu cầu quên mật khẩu cho admin không tồn tại: {}", email);
            return; 
        }

        // 2. Sinh mã Token 15 phút
        String resetToken = java.util.UUID.randomUUID().toString();
        long expirationTimeMillis = System.currentTimeMillis() + (15 * 60 * 1000);
        
        // Cần đảm bảo Entity User có 2 trường resetPasswordToken và resetPasswordExpiry
        userModel.setResetPasswordToken(resetToken);
        userModel.setResetPasswordExpiry(new Date(expirationTimeMillis));
        userService.update(userModel);

        // 3. Gửi Email (Cần cấu hình giao diện email xanh dương cho Admin)
        try {
            emailService.sendUserResetPasswordEmail(userModel.getAdminEmail(), resetToken);
        } catch (Exception e) {
            LOGGER.error("Lỗi gửi email admin: ", e);
            throw new Exception("Không thể gửi email lúc này.");
        }
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) throws Exception {
        // 1. Tìm User (Admin) dựa vào mã Token
        // Đảm bảo UserService và UserRepository của bạn đã có hàm này
        User userModel = userService.getByPasswordResetToken(token); 
        
        if (userModel == null) {
            throw new IllegalArgumentException("Đường dẫn khôi phục không hợp lệ hoặc đã được sử dụng.");
        }

        // 2. Kiểm tra thời hạn sử dụng của Token (15 phút)
        if (userModel.getResetPasswordExpiry() == null || userModel.getResetPasswordExpiry().before(new java.util.Date())) {
            throw new IllegalArgumentException("Đường dẫn khôi phục đã hết hạn. Vui lòng yêu cầu đường dẫn mới.");
        }

        // 3. Mã hóa mật khẩu mới 
        // (Lưu ý: Đảm bảo bạn đã Inject 'PasswordEncoder' vào Constructor của UserFacadeImpl)
        String encodedPassword = passwordEncoder.encode(newPassword);
        userModel.setAdminPassword(encodedPassword); // Chú ý: Entity User dùng 'adminPassword' hoặc 'password' tùy thiết kế DB của bạn

        // 4. Mở khóa tài khoản, dọn dẹp số lần đăng nhập sai và xóa Token
        userModel.setActive(true);
        userModel.setFailedLoginAttempts(0);
        userModel.setLockTime(null);
        userModel.setResetPasswordToken(null);
        userModel.setResetPasswordExpiry(null);

        // 5. Cập nhật xuống Database
        userService.update(userModel); // Hoặc userService.saveOrUpdate(userModel) tùy theo Interface của bạn
    }
}