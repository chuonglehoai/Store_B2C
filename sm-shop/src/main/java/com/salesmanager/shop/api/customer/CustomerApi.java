package com.salesmanager.shop.api.customer;

import java.security.Principal;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.shop.facade.customer.CustomerFacade;
import com.salesmanager.shop.model.customer.ReadableCustomer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Management", description = "APIs quản lý khách hàng B2C")
public class CustomerApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerApi.class);

    private final CustomerFacade customerFacade;

    public CustomerApi(CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    // 1. Lấy thông tin khách hàng cá nhân (Profile đang đăng nhập)
    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin hồ sơ của khách hàng hiện tại")
    public ResponseEntity<?> getProfile(Principal principal) throws Exception { // <-- Thêm throws Exception ở đây
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Chưa đăng nhập!"));
        }
        Customer customer = customerFacade.getByNick(principal.getName());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy khách hàng"));
        }
        return ResponseEntity.ok(customer);
    }

    // 2. Lấy thông tin khách hàng theo ID (Dành cho Admin tra cứu)
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết khách hàng theo ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Customer customer = customerFacade.getById(id);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Khách hàng không tồn tại với ID: " + id));
        }
        return ResponseEntity.ok(customer);
    }

    // 3. Lấy danh sách khách hàng có phân trang
    @GetMapping
    @Operation(summary = "Lấy danh sách khách hàng có phân trang")
    public ResponseEntity<Page<Customer>> list(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email) {

        CustomerCriteria criteria = new CustomerCriteria();
        criteria.setStartPage(page);
        criteria.setPageSize(size);
        criteria.setName(name);
        criteria.setEmail(email);

        Page<Customer> customerPage = customerFacade.listByCriteria(criteria, page, size);
        return ResponseEntity.ok(customerPage);
    }

    // 4. Xóa khách hàng theo ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tài khoản khách hàng theo ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            customerFacade.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công khách hàng có ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}