package com.salesmanager.shop.facade.customer;

import org.springframework.data.domain.Page;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;

public interface CustomerFacade {

    // 1. Truy xuất dữ liệu
    Customer getById(Long id);

    Customer getByNick(String nick);

    Customer getByEmail(String email);

    Customer getByUserName(String uername);

    Page<Customer> listByCriteria(CustomerCriteria criteria, int page, int count);

    // 2. Đăng ký & Tạo mới
    ReadableCustomer registerCustomer(PersistableCustomer customer) throws Exception;
    // 3. Đăng nhập & Xác thực
    boolean authenticate(String username, String rawPassword);
    public boolean checkIfUserExists(final String userName) throws Exception;

    //  Cập nhật thông tin
    ReadableCustomer updateCustomer(Long id, PersistableCustomer customer) throws Exception;
    // 4. Xóa tài khoản
    void deleteById(Long id);

    void deleteByNick(String nick);
    
    void requestPasswordReset(String email) throws Exception;
    void resetPasswordWithToken(String token, String newPassword) throws Exception;
}