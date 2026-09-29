package com.salesmanager.core.business.services.email;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.model.customer.Customer;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender, CustomerService customerService, PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    // Đã xóa bỏ tham số verificationToken
    @Override
    public void sendRegisterEmail(String toEmail, String customerName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký thành công - Chào mừng đến với B2C Store!");
            
            // Thiết kế lại giao diện HTML mang tính chào mừng
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2 style='color: #007bff;'>Chào " + customerName + ",</h2>"
                    + "<p>Chúc mừng bạn đã tạo thành công tài khoản tại <strong>B2C Store</strong> với địa chỉ email: <strong>" + toEmail + "</strong>.</p>"
                    + "<p>Giờ đây bạn đã có thể đăng nhập để quản lý đơn hàng, lưu trữ sản phẩm yêu thích và nhận các ưu đãi dành riêng cho thành viên.</p>"
                    + "<br>"
                    + "<p style='margin-top: 30px;'><small>Nếu bạn cần hỗ trợ, vui lòng trả lời trực tiếp email này hoặc liên hệ bộ phận Chăm sóc khách hàng.</small></p>"
                    + "<p><small>Trân trọng,<br><strong>Đội ngũ B2C Store</strong></small></p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            LOGGER.info("Đã gửi email thông báo đăng ký thành công tới: {}", toEmail);

        } catch (Exception e) {
            LOGGER.error("Lỗi khi gửi email tới {}: ", toEmail, e);
        }
    }

    @Override
    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        try {
            String resetLink = "http://127.0.0.1:5500/forgot-password.html?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu khôi phục mật khẩu - B2C Store");
            helper.setText("Chào bạn,\n\n"
                    + "Tài khoản của bạn đã bị khóa hoặc có yêu cầu khôi phục mật khẩu.\n"
                    + "Vui lòng click vào đường link dưới đây để thiết lập lại mật khẩu mới:\n\n"
                    + resetLink + "\n\n"
                    + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n"
                    + "Trân trọng,\nB2C Store Team");

            mailSender.send(message);
            LOGGER.info("Đã gửi email khôi phục mật khẩu tới: {}", toEmail);

        } catch (Exception e) {
            LOGGER.error("Lỗi khi gửi email tới {}: ", toEmail, e);
        }
    }
    
    @Override
    public void sendUserResetPasswordEmail(String toEmail, String resetToken) {
        try {
            // Đảm bảo link trỏ đúng về trang khôi phục của Admin 
            String resetLink = "http://127.0.0.1:5500/user/user-reset-password.html?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[Clarity Admin] Yêu cầu thiết lập lại mật khẩu bảo mật");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2 style='color: #0053db;'>Cảnh báo bảo mật nội bộ</h2>"
                    + "<p>Chào Quản trị viên,</p>"
                    + "<p>Hệ thống ghi nhận yêu cầu khôi phục mật khẩu hoặc tài khoản của bạn đã bị tạm khóa. "
                    + "Để thiết lập lại mật khẩu truy cập Bảng điều khiển, vui lòng nhấp vào đường dẫn bảo mật dưới đây:</p>"
                    + "<br>"
                    + "<a href='" + resetLink + "' style='display: inline-block; padding: 10px 20px; color: #fff; background-color: #0053db; text-decoration: none; border-radius: 5px; font-weight: bold;'>Thiết lập mật khẩu mới</a>"
                    + "<p style='margin-top: 20px;'><small>Đường dẫn này chỉ có hiệu lực trong 15 phút. Nếu bạn không thực hiện yêu cầu này, vui lòng báo cáo ngay cho SuperAdmin.</small></p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            LOGGER.info("Đã gửi email khôi phục mật khẩu Quản trị tới: {}", toEmail);

        } catch (Exception e) {
            LOGGER.error("Lỗi khi gửi email Admin tới {}: ", toEmail, e);
        }
    }
    
}