package com.salesmanager.shop.model.user;

public class UserEntity extends User {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String adminName;
  private String emailAddress;
  private String adminPhone;
  private String avatarUrl;
  private String adminAddress;
  private String adminUserName;
	private boolean active;
  

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getAdminPhone() {
    return adminPhone;
  }

  public void setAdminPhone(String adminPhone) {
    this.adminPhone = adminPhone;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getAdminAddress() {
    return adminAddress;
  }

  public void setAdminAddress(String adminAddress) {
    this.adminAddress = adminAddress;
  } 

  public String getAdminUserName() {
    return adminUserName;
  }

  public void setAdminUserName(String adminUserName) {
    this.adminUserName = adminUserName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

}
