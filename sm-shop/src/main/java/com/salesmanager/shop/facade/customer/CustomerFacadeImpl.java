package com.salesmanager.shop.facade.customer;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.group.GroupService;
import com.salesmanager.core.business.services.email.EmailService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.core.model.customer.CustomerList;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.api.exception.UserAlreadyExistException;

@Service("customerFacade")
public class CustomerFacadeImpl implements CustomerFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerFacadeImpl.class);

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GroupService groupService;

    public CustomerFacadeImpl(CustomerService customerService, PasswordEncoder passwordEncoder, EmailService emailService, GroupService groupService) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.groupService = groupService;
    }


    @Override
    public Customer getById(Long id) {
        return customerService.getById(id);
    }

    @Override
    public Customer getByNick(String nick) {
        return customerService.getByNick(nick);
    }

    @Override
    public Customer getByEmail(String email) {
        return customerService.getByEmail(email);
    }

    @Override
    public Customer getByUserName(String userName) {
        return customerService.getByUserName(userName);
    }

    @Override
    public Page<Customer> listByCriteria(CustomerCriteria criteria, int page, int count) {
        return customerService.listByCriteria(criteria, page, count);
    }

    @Override
    public boolean checkIfUserExists(final String userName)
        throws Exception {
        if (StringUtils.isNotBlank(userName)) {
        Customer customer = customerService.getByNick(userName);
        if (customer != null) {
            LOG.info("Customer with userName {} already exists for store {} ", userName);
            return true;
        }

        LOG.info("No customer found with userName {} for store {} ", userName);
        return false;

        }
        LOG.info("Either userName is empty or we have not found any value for store");
        return false;
    }

    // --- 2. ĐĂNG KÝ TÀI KHOẢN ---

    @Override
    public ReadableCustomer registerCustomer(PersistableCustomer customer) throws Exception {
        LOG.info("Registering new customer with email: {}", customer.getEmailAddress());

        // 1. Đồng bộ định danh (An toàn kép phòng trường hợp API chưa gán)
        if (customer.getUserName() == null) {
            customer.setUserName(customer.getEmailAddress());
        }

        // 2. Kiểm tra trùng lặp bằng userName (đã chứa email)
        // Dùng UserAlreadyExistException để Spring Boot tự động map ra mã lỗi 409 Conflict trả về cho app
        if (customerService.getByNick(customer.getUserName()) != null) {
            throw new UserAlreadyExistException("Tài khoản với email [" + customer.getEmailAddress() + "] đã tồn tại.");
        }

        // 3. Chuyển đổi từ DTO sang Entity 
        // (getCustomerModel sẽ lo toàn bộ việc mã hóa mật khẩu và gán Group mặc định)
        Customer customerModel = getCustomerModel(customer);

        customerModel.setActive(true); 

        List<Group> groups = groupService.listGroup(GroupType.CUSTOMER);
        for (Group group : groups) {
            if (group.getGroupName().equals("CUSTOMER")) { 
                customerModel.getGroups().add(group);
                break;
            }
        }

        // 4. Lưu xuống Database
        customerService.saveOrUpdate(customerModel);

        // 5. Chuyển đổi Entity sang DTO an toàn (ReadableCustomer) để trả về cho Client
        return convertCustomerToReadableCustomer(customerModel);
    }

    
    
    // --- HÀM HELPER CHUYỂN ĐỔI DTO <-> ENTITY ---

    private Customer getCustomerModel(PersistableCustomer customer) {
        Customer customerModel = new Customer();

        customerModel.setFullName(customer.getFullName());
        customerModel.setUserName(customer.getUserName());
        customerModel.setNick(customer.getNick());
        customerModel.setEmailAddress(customer.getEmailAddress());
        
        customerModel.setDateOfBirth(customer.getDateOfBirth());
        customerModel.setGender(customer.getGender() != null ? 
                com.salesmanager.core.model.customer.CustomerGender.valueOf(customer.getGender()) : null);
        customerModel.setTelephone(customer.getTelephone());
        customerModel.setAddress(customer.getAddress());
        customerModel.setAvatarUrl(customer.getAvatarUrl());
        customerModel.setActive(customer.isActive());
        // Mã hóa mật khẩu
        if (StringUtils.isNotBlank(customer.getPassword())) {
            customerModel.setPassword(passwordEncoder.encode(customer.getPassword()));
        }

        if (customer.getGender() != null) {
            try {
                customerModel.setGender(com.salesmanager.core.model.customer.CustomerGender.valueOf(customer.getGender()));
            } catch (Exception ignored) {}
        }
        
        return customerModel;
    }

    private ReadableCustomer convertCustomerToReadableCustomer(Customer customerModel) {
        ReadableCustomer readableCustomer = new ReadableCustomer();
        readableCustomer.setId(customerModel.getId());
        readableCustomer.setEmailAddress(customerModel.getEmailAddress());
        readableCustomer.setUserName(customerModel.getNick());
        return readableCustomer;
    }

    // --- 3. ĐĂNG NHẬP & XÁC THỰC ---

    @Override
    public boolean authenticate(String username, String rawPassword) {
        Customer customer = customerService.getByNick(username);
        if (customer == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, customer.getPassword());
    }

    @Override
    public ReadableCustomer updateCustomer(Long id, PersistableCustomer customer) throws Exception {
        LOG.info("Updating profile for customer ID: {}", id);

        // 1. Tìm tài khoản gốc trong Database
        Customer customerModel = customerService.getById(id);
        if (customerModel == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
        }

        // 2. Nếu khách hàng muốn đổi Email, cần kiểm tra trùng lặp với người khác
        if (StringUtils.isNotBlank(customer.getEmailAddress()) 
                && !customer.getEmailAddress().equalsIgnoreCase(customerModel.getEmailAddress())) {
            
            if (customerService.getByNick(customer.getEmailAddress()) != null) {
                throw new UserAlreadyExistException("Tài khoản với email [" + customer.getEmailAddress() + "] đã được người khác sử dụng.");
            }
            customerModel.setEmailAddress(customer.getEmailAddress());
            customerModel.setUserName(customer.getEmailAddress()); // Đồng bộ userName
        }

        // 3. Đắp dữ liệu mới từ DTO sang Entity (Bỏ qua mật khẩu)
        if (StringUtils.isNotBlank(customer.getFullName())) {
            customerModel.setFullName(customer.getFullName());
        }
        if (StringUtils.isNotBlank(customer.getNick())) {
            customerModel.setNick(customer.getNick());
        }
        if (customer.getDateOfBirth() != null) {
            customerModel.setDateOfBirth(customer.getDateOfBirth());
        }
        if (StringUtils.isNotBlank(customer.getGender())) {
            try {
                customerModel.setGender(com.salesmanager.core.model.customer.CustomerGender.valueOf(customer.getGender()));
            } catch (Exception ignored) {
                LOG.warn("Giới tính không hợp lệ: {}", customer.getGender());
            }
        }
        if (StringUtils.isNotBlank(customer.getTelephone())) {
            customerModel.setTelephone(customer.getTelephone());
        }
        if (StringUtils.isNotBlank(customer.getAddress())) {
            customerModel.setAddress(customer.getAddress());
        }
        if (StringUtils.isNotBlank(customer.getAvatarUrl())) {
            customerModel.setAvatarUrl(customer.getAvatarUrl());
        }
        
        customerModel.setActive(customer.isActive());

        // 4. Lưu xuống Database
        customerService.saveOrUpdate(customerModel);

        // 5. Trả về kết quả cho API
        return convertCustomerToReadableCustomer(customerModel);
    }

    @Override
    public void deleteById(Long id) {
        Customer customer = getById(id);
        if (customer != null) {
            try {
                customerService.delete(customer);
            } catch (ServiceException e) {
                LOG.error("Lỗi khi xóa khách hàng id: {}", id, e);
                throw new RuntimeException("Không thể xóa tài khoản!", e);
            }
        }
    }

    @Override
    public void deleteByNick(String nick) {
        Customer customer = getByNick(nick);
        if (customer != null) {
            try {
                customerService.delete(customer);
            } catch (ServiceException e) {
                LOG.error("Lỗi khi xóa khách hàng nick: {}", nick, e);
                throw new RuntimeException("Không thể xóa tài khoản!", e);
            }
        }
    }

    

    @Override
    public void requestPasswordReset(String email) throws Exception {
        // 1. Tìm khách hàng theo email
        Customer customerModel = customerService.getByEmail(email); 
        
        if (customerModel == null) {
            LOG.warn("Yêu cầu quên mật khẩu cho email không tồn tại: {}", email);
            return; 
        }

        // 2. Sinh mã Token dùng 1 lần
        String resetToken = java.util.UUID.randomUUID().toString();
        
        // 3. Tính toán thời gian hết hạn (Ví dụ: 15 phút kể từ hiện tại)
        long expirationTimeMillis = System.currentTimeMillis() + (15 * 60 * 1000);
        Date expiryDate = new Date(expirationTimeMillis);

        // 4. Lưu vào Database
        customerModel.setResetPasswordToken(resetToken);
        customerModel.setResetPasswordExpiry(expiryDate);
        customerService.saveOrUpdate(customerModel);

        // 5. Gửi Email cho khách hàng
        try {
            emailService.sendResetPasswordEmail(customerModel.getEmailAddress(), resetToken);
        } catch (Exception e) {
            LOG.error("Lỗi khi gửi email khôi phục mật khẩu cho: {}", email, e);
            throw new RuntimeException("Không thể gửi email lúc này. Vui lòng thử lại sau.");
        }
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) throws Exception {
        // 1. Tìm khách hàng sở hữu mã Token này thông qua Service
        Customer customerModel = customerService.getByPasswordResetToken(token);
        
        if (customerModel == null) {
            throw new IllegalArgumentException("Đường dẫn khôi phục không hợp lệ hoặc đã được sử dụng.");
        }

        // 2. Kiểm tra thời hạn của Token (Xem đã quá 15 phút chưa)
        if (customerModel.getResetPasswordExpiry() == null || customerModel.getResetPasswordExpiry().before(new Date())) {
            throw new IllegalArgumentException("Đường dẫn khôi phục đã hết hạn. Vui lòng yêu cầu một đường dẫn mới.");
        }

        // 3. Mã hóa mật khẩu mới bằng Bcrypt
        String encodedPassword = passwordEncoder.encode(newPassword);
        customerModel.setPassword(encodedPassword);

        // 4. Mở khóa tài khoản và dọn dẹp lịch sử phạt
        // Đảm bảo khách hàng có thể đăng nhập lại nếu trước đó bị khóa do sai pass 5 lần
        customerModel.setActive(true);
        customerModel.setFailedLoginAttempts(0);
        customerModel.setLockTime(null);
        
        // Cực kỳ quan trọng: Xóa Token để link này không thể bị tái sử dụng
        customerModel.setResetPasswordToken(null);
        customerModel.setResetPasswordExpiry(null);

        // 5. Cập nhật xuống Database
        customerService.saveOrUpdate(customerModel);
        
        LOG.info("Khách hàng {} đã đặt lại mật khẩu thành công bằng Token.", customerModel.getEmailAddress());
    }
    
}