package com.salesmanager.shop.security.user;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.model.customer.Customer;

@Service("jwtCustomerDetailsService")
public class JWTCustomerDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTCustomerDetailsService.class);
    
    private final CustomerService customerService;

    public JWTCustomerDetailsService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Truy vấn vào bảng Customer
            Customer customer = customerService.getByUserName(username);
            
            if (customer == null) {
                LOGGER.warn("Không tìm thấy Khách hàng: {}", username);
                throw new UsernameNotFoundException("Không tìm thấy Khách hàng: " + username);
            }
            
            LOGGER.info("Đã tìm thấy Customer [{}], chuẩn bị tạo JWTUser", username);

            return new JWTUser(
                customer.getId(),                  
                customer.getUserName(),
                customer.getFullName(),            
                customer.getEmailAddress(),        
                customer.getPassword(),            
                Collections.emptyList(),           
                customer.isActive(),               
                null                               
            );
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Lỗi khi load Customer [{}]: ", username, e);
            throw new UsernameNotFoundException("Lỗi truy vấn hệ thống", e);
        }
    }
}