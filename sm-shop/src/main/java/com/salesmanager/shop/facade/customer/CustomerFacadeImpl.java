package com.salesmanager.shop.facade.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.core.model.customer.CustomerList;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.api.exception.UserAlreadyExistException;

@Service("customerFacade")
public class CustomerFacadeImpl implements CustomerFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerFacadeImpl.class);

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    public CustomerFacadeImpl(CustomerService customerService, PasswordEncoder passwordEncoder) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. TRUY XUẤT DỮ LIỆU ---

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

    // --- 4. XÓA TÀI KHOẢN ---

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

    // --- 5. ĐỔI / ĐẶT LẠI MẬT KHẨU ---

    @Override
    public void changePassword(Long customerId, String newPassword) {
        Customer customer = getById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        try {
            customerService.saveOrUpdate(customer);
        } catch (ServiceException e) {
            LOG.error("Lỗi cập nhật mật khẩu cho ID: {}", customerId, e);
            throw new RuntimeException("Không thể đổi mật khẩu!", e);
        }
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        Customer customer = customerService.getByPasswordResetToken(token);
        if (customer == null) {
            throw new IllegalArgumentException("Mã Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn!");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        // Xóa token sau khi đã đổi mật khẩu thành công
        customer.setCredentialsResetRequest(null);
        try {
            customerService.saveOrUpdate(customer);
        } catch (ServiceException e) {
            LOG.error("Lỗi cập nhật mật khẩu theo token", e);
            throw new RuntimeException("Không thể đặt lại mật khẩu!", e);
        }
    }
}