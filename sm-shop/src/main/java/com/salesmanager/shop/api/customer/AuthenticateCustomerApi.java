package com.salesmanager.shop.api.customer;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.email.EmailService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.shop.facade.customer.CustomerFacade;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.security.AuthenticationRequest;
import com.salesmanager.shop.security.AuthenticationResponse;
import com.salesmanager.shop.security.JWTTokenUtil;
import com.salesmanager.shop.security.user.JWTUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Api(tags = {"Customer authentication resource"})
public class AuthenticateCustomerApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateCustomerApi.class);

    // 1. Chuyển tất cả các bean thành hằng số (private final) và XÓA BỎ @Autowired
    private final AuthenticationManager jwtCustomerAuthenticationManager;
    private final JWTTokenUtil jwtTokenUtil;
    private final UserDetailsService jwtCustomerDetailsService;
    private final CustomerFacade customerFacade;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final EmailService emailService;

    // 2. Gộp tất cả vào 1 Hàm khởi tạo (Constructor Injection)
    // Spring Boot sẽ tự động quét hàm này và bơm (inject) tất cả các bean vào đây
    public AuthenticateCustomerApi(
            AuthenticationManager jwtCustomerAuthenticationManager,
            JWTTokenUtil jwtTokenUtil,
            @Qualifier("jwtCustomerDetailsService") UserDetailsService jwtCustomerDetailsService,
            CustomerFacade customerFacade,
            PasswordEncoder passwordEncoder,
            CustomerService customerService,
            EmailService emailService) {
        this.jwtCustomerAuthenticationManager = jwtCustomerAuthenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtCustomerDetailsService = jwtCustomerDetailsService;
        this.customerFacade = customerFacade;
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
        this.emailService = emailService;
    }
    /**
     * Đăng ký tài khoản Customer mới
     */
    @PostMapping(value = "/customers/register", produces = { "application/json" })
    @ApiOperation(httpMethod = "POST", value = "Registers a customer to the application", response = AuthenticationResponse.class)
    public ResponseEntity<?> register(@Valid @RequestBody PersistableCustomer customer) {
        try {
            Validate.notNull(customer.getEmailAddress(), "Email không được để trống");
            Validate.notNull(customer.getPassword(), "Mật khẩu không được để trống");
            Validate.notNull(customer.getRepeatPassword(), "Mật khẩu nhập lại không được để trống");

            customer.setUserName(customer.getEmailAddress());
            String rawPassword = customer.getPassword();

            if (!customer.getPassword().equals(customer.getRepeatPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Mật khẩu nhập lại không khớp."));
            }

            if (customerFacade.checkIfUserExists(customer.getEmailAddress())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Tài khoản với email này đã tồn tại"));
            }

            ReadableCustomer readableCustomer = customerFacade.registerCustomer(customer);

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
            Customer customerModel = customerService.getByUserName(authenticationRequest.getUsername());

            if (customerModel == null) {
                throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
            }

            if (!customerModel.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Tài khoản đã bị khóa bảo mật. Vui lòng kiểm tra Email để khôi phục."));
            }

            // 3. Kiểm tra KHÓA TẠM THỜI (15 phút)
            int currentFails = customerModel.getFailedLoginAttempts() == null ? 0 : customerModel.getFailedLoginAttempts();
            if (currentFails >= 5 && customerModel.getLockTime() != null) {
                long lockDurationMillis = System.currentTimeMillis() - customerModel.getLockTime().getTime();
                long fifteenMinutesMillis = 15 * 60 * 1000;
                
                if (lockDurationMillis < fifteenMinutesMillis) {
                    long minutesLeft = (fifteenMinutesMillis - lockDurationMillis) / 60000;
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(Map.of("error", "Tài khoản bị khóa tạm thời. Vui lòng thử lại sau " + minutesLeft + " phút."));
                }
            }
            // 1. Tải thông tin người dùng từ đúng bảng Customer
            final JWTUser userDetails = (JWTUser) jwtCustomerDetailsService.loadUserByUsername(authenticationRequest.getUsername());

            // 2. Tự so sánh mật khẩu bằng PasswordEncoder
            if (!passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
                currentFails++;
                customerModel.setFailedLoginAttempts(currentFails);
                if (currentFails == 5) {
                    // Phạt lần 1: Khóa 15 phút
                    customerModel.setLockTime(new Date());
                    customerService.update(customerModel);
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(Map.of("error", "Nhập sai mật khẩu 5 lần. Tài khoản bị khóa tạm thời 15 phút."));
                } else if (currentFails > 5) {
                    // Phạt lần 2 (Sau khi đã qua 15 phút mà vẫn sai): Khóa vĩnh viễn
                    customerModel.setActive(false);
                    customerService.update(customerModel);
                    
                    try {
                        customerFacade.requestPasswordReset(customerModel.getEmailAddress());
                    } catch (Exception ex) {
                        LOGGER.error("Lỗi khi gửi email khôi phục cho tài khoản bị khóa: ", ex);
                    }
                    
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Tài khoản đã bị khóa vĩnh viễn do nhập sai quá nhiều lần. Kiểm tra Email."));
                }
                customerService.update(customerModel);
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

    /**
     * API 1: Yêu cầu gửi link khôi phục mật khẩu
     */
    @PostMapping(value = "/customers/password/reset/request", produces = { "application/json" })
    @ApiOperation(httpMethod = "POST", value = "Gửi email chứa link khôi phục mật khẩu")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (org.apache.commons.lang3.StringUtils.isBlank(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vui lòng cung cấp địa chỉ email."));
            }

            // Gọi Facade xử lý mọi logic
            customerFacade.requestPasswordReset(email);

            // Luôn trả về thông báo thành công dù email có thật hay không (Chống dò quét tài khoản)
            return ResponseEntity.ok(Map.of("message", "Nếu email hợp lệ, một đường link khôi phục mật khẩu đã được gửi đến hộp thư của bạn."));

        } catch (Exception e) {
            LOGGER.error("Lỗi hệ thống khi yêu cầu khôi phục mật khẩu: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * API 2: Đặt lại mật khẩu bằng Token
     */
    @PostMapping(value = "/customers/password/reset", produces = { "application/json" })
    @ApiOperation(httpMethod = "POST", value = "Đặt lại mật khẩu mới thông qua Token")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String token = requestBody.get("token");
            String newPassword = requestBody.get("newPassword");

            if (org.apache.commons.lang3.StringUtils.isBlank(token) || org.apache.commons.lang3.StringUtils.isBlank(newPassword)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Thiếu mã xác thực hoặc mật khẩu mới."));
            }

            // Gọi Facade xử lý: Check token, check hạn, băm mật khẩu và lưu DB
            customerFacade.resetPasswordWithToken(token, newPassword);

            return ResponseEntity.ok(Map.of("message", "Thiết lập mật khẩu mới thành công! Bạn có thể đăng nhập ngay bây giờ."));

        } catch (IllegalArgumentException e) {
            // Bắt lỗi Token sai hoặc hết hạn từ Facade ném ra
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Lỗi hệ thống khi đặt lại mật khẩu: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi máy chủ: " + e.getMessage()));
        }
    }
}