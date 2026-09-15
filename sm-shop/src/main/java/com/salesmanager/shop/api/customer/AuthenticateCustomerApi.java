package com.salesmanager.shop.api.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.commons.lang3.Validate;

import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.api.exception.GenericRuntimeException;
import com.salesmanager.shop.facade.customer.CustomerFacade;
import com.salesmanager.shop.security.AuthenticationRequest;
import com.salesmanager.shop.security.AuthenticationResponse;
import com.salesmanager.shop.security.JWTTokenUtil;
import com.salesmanager.shop.security.user.JWTUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1")
@Api(tags = {"Customer authentication resource"})
public class AuthenticateCustomerApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateCustomerApi.class);

    @Autowired
    private AuthenticationManager jwtCustomerAuthenticationManager;

    @Autowired
    private JWTTokenUtil jwtTokenUtil;

    @Autowired
    @Qualifier("jwtCustomerDetailsService")
    private UserDetailsService jwtCustomerDetailsService;

    @Autowired
    private CustomerFacade customerFacade;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * Đăng ký tài khoản Customer mới
     */
    @PostMapping(value = "/customers/register", produces = { "application/json" })
    @ApiOperation(httpMethod = "POST", value = "Registers a customer to the application", response = AuthenticationResponse.class)
    public ResponseEntity<?> register(@Valid @RequestBody PersistableCustomer customer) {
        try {
            Validate.notNull(customer.getEmailAddress(), "Email không được để trống");
            customer.setUserName(customer.getEmailAddress());
            
            // Giữ lại mật khẩu gốc vì có thể customer bị Facade mã hóa mất
            Validate.notNull(customer.getPassword(), "Mật khẩu không được để trống");
            String rawPassword = customer.getPassword();
            if (!customer.getPassword().equals(customer.getRepeatPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Mật khẩu nhập lại không khớp."));
            }

            // Kiểm tra tồn tại
            if (customerFacade.checkIfUserExists(customer.getEmailAddress())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Tài khoản với email này đã tồn tại"));
            }

            // 1. Lưu thông tin khách hàng vào Database (BƯỚC NÀY ĐÃ THÀNH CÔNG)
            ReadableCustomer readableCustomer = customerFacade.registerCustomer(customer);
                try {
                    LOGGER.info("=== BẮT ĐẦU TEST XÁC THỰC ===");
                    // 1. Test thử tự tìm User trong DB
                    UserDetails debugUser = jwtCustomerDetailsService.loadUserByUsername(customer.getUserName());
                    LOGGER.info("1. Đã tìm thấy Customer: {}", debugUser.getUsername());
                    
                    // 2. In ra mật khẩu
                    LOGGER.info("2. Mật khẩu Hash trong DB: {}", debugUser.getPassword());
                    LOGGER.info("3. Mật khẩu Raw đang nhập: {}", rawPassword);
                    
                    // 3. Test thử tự so sánh mật khẩu
                    boolean isMatch = passwordEncoder.matches(rawPassword, debugUser.getPassword());
                    LOGGER.info("4. Hai mật khẩu có khớp nhau không?: {}", isMatch);
                    LOGGER.info("=== KẾT THÚC TEST XÁC THỰC ===");
                } catch (Exception e) {
                    LOGGER.error("LỖI DEBUG: Không thể load user từ DB", e);
                }
            final JWTUser userDetails = (JWTUser) jwtCustomerDetailsService.loadUserByUsername(customer.getUserName());
            
            if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }

            // Tạo đối tượng Authentication thủ công sau khi tự xác thực thành công
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Sinh JWT Token
            final String token = jwtTokenUtil.generateToken(userDetails);

            // Trả về ID và Token
            return ResponseEntity.ok(new AuthenticationResponse(readableCustomer.getId(), token));

        } catch (BadCredentialsException e) {
            LOGGER.error("Sai mật khẩu khi auto-login: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Đăng ký thành công nhưng đăng nhập tự động thất bại."));
        } catch (ClassCastException e) {
            LOGGER.error("Lỗi ép kiểu UserDetailsService: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Lỗi cấu hình JWTUser (ClassCastException)"));
        } catch (Exception e) {
            LOGGER.error("Lỗi văng ra sau khi lưu Database: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đăng nhập tài khoản Customer
     */
    /**
     * Đăng nhập tài khoản Customer
     */
    @PostMapping(value = "/customers/login", produces = { "application/json" })
    @ApiOperation(httpMethod = "POST", value = "Authenticates a customer to the application", response = AuthenticationResponse.class)
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {

        try {
            // 1. Tải thông tin người dùng từ đúng bảng Customer
            final JWTUser userDetails = (JWTUser) jwtCustomerDetailsService.loadUserByUsername(authenticationRequest.getUsername());

            // 2. Tự so sánh mật khẩu bằng PasswordEncoder
            if (!passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
                throw new BadCredentialsException("Sai mật khẩu");
            }

            // 3. Tạo đối tượng Authentication thủ công để báo cho Spring biết đã đăng nhập thành công
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. Sinh JWT Token
            final String token = jwtTokenUtil.generateToken(userDetails);

            // 5. Trả về kết quả
            return ResponseEntity.ok(new AuthenticationResponse(userDetails.getId(), token));

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            LOGGER.warn("Đăng nhập thất bại (Sai mật khẩu/Tài khoản): {}", authenticationRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
            
        } catch (Exception e) {
            LOGGER.error("Lỗi hệ thống khi đăng nhập cho user [{}]: ", authenticationRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi máy chủ: " + e.getMessage()));
        }
    }
}