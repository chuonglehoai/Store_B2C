package com.salesmanager.core.business.services.email;

import com.salesmanager.core.model.customer.Customer;

public interface EmailService {
    void sendRegisterEmail(String toEmail, String customerName);
    void sendResetPasswordEmail(String toEmail, String resetToken);
    void sendUserResetPasswordEmail(String toEmail, String resetToken);
}