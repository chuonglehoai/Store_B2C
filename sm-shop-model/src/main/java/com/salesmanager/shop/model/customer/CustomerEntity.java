package com.salesmanager.shop.model.customer;

import java.io.Serializable;
import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class CustomerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty(notes = "Customer full name")
    @NotBlank(message = "Tên tài khoản không được để trống")
    private String fullName;

    @ApiModelProperty(notes = "Customer email address. Required for registration")
    @Email(message = "Định dạng email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String emailAddress;

    @ApiModelProperty(notes = "Username (used for login)")
    private String userName;

    @ApiModelProperty(notes = "Tên tài khoản")
    @NotBlank(message = "Tên tài khoản không được để trống")
    private String nick;

    @ApiModelProperty(notes = "Customer gender M | F")
    private String gender;

    @ApiModelProperty(notes = "Customer date of birth")
    private Date dateOfBirth;

    @ApiModelProperty(notes = "Customer avatar URL")
    private String avatarUrl;

    @ApiModelProperty(notes = "Customer full address")
    private String address;

    @ApiModelProperty(notes = "Customer telephone number")
    private String telephone;

    @ApiModelProperty(notes = "Account active status")
    private boolean active = true;

    // --- GETTERS & SETTERS (Đã chuẩn hóa, không trùng lặp) ---

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getFullName() { 
        return fullName; 
    }

    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }

    public String getEmailAddress() { 
        return emailAddress; 
    }

    public void setEmailAddress(String emailAddress) { 
        this.emailAddress = emailAddress; 
    }

    public String getUserName() { 
        return userName; 
    }

    public void setUserName(String userName) { 
        this.userName = userName; 
    }

    public String getNick() { 
        return nick; 
    }

    public void setNick(String nick) { 
        this.nick = nick; 
    }

    public String getGender() { 
        return gender; 
    }

    public void setGender(String gender) { 
        this.gender = gender; 
    }

    public Date getDateOfBirth() { 
        return dateOfBirth; 
    }

    public void setDateOfBirth(Date dateOfBirth) { 
        this.dateOfBirth = dateOfBirth; 
    }

    public String getAvatarUrl() { 
        return avatarUrl; 
    }

    public void setAvatarUrl(String avatarUrl) { 
        this.avatarUrl = avatarUrl; 
    }

    public String getAddress() { 
        return address; 
    }

    public void setAddress(String address) { 
        this.address = address; 
    }

    public String getTelephone() { 
        return telephone; 
    }

    public void setTelephone(String telephone) { 
        this.telephone = telephone; 
    }

    public boolean isActive() { 
        return active; 
    }

    public void setActive(boolean active) { 
        this.active = active; 
    }
}