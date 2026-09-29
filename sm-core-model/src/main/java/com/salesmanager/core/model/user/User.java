package com.salesmanager.core.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.Cascade;

import com.salesmanager.core.model.common.CredentialsReset;
import com.salesmanager.core.model.common.audit.AuditListener;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "USERS", 
    //indexes = { @Index(name="USR_NAME_IDX", columnList = "ADMIN_NAME")},
	uniqueConstraints=
	@UniqueConstraint(columnNames = { "ADMIN_NAME"}))
public class User extends SalesManagerEntity<Long, User> implements Auditable {
	
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "USER_ID", unique=true, nullable=false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public User() {
		
	}
	
	public User(String userName,String password, String email) {
		
		this.adminName = userName;
		this.adminPassword = password;
		this.adminEmail = email;
	}
	
	@NotBlank(message = "Tên đăng nhập không được để trống")
	@Column(name="ADMIN_NAME", length=100)
	private String adminName;
	
	@NotEmpty(message = "Phân quyền (Group) cho người dùng không được để trống")
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
	@JoinTable(
		name = "USER_GROUP",
		joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID"),
		inverseJoinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID")
	)
	private Set<Group> groups = new HashSet<>();
	
	@NotBlank(message = "Email không được để trống")
	@Email
	@Column(name="ADMIN_EMAIL")
	private String adminEmail;
	
	@NotBlank(message = "Mật khẩu không được để trống")
	@Column(name="ADMIN_PASSWORD", length=60)
	private String adminPassword;

	@Column(name="ADMIN_USER_NAME")
	private String adminUserName;
	
	@Column(name="ACTIVE")
	private boolean active = true;

	@Column(name="ADMIN_PHONE", length=20)
    private String adminPhone;

    @Column(name="ADMIN_AVATAR")
    private String avatarUrl;

	@Column(name="ADMIN_ADDRESS", length=255)
	private String adminAddress;
	

	@Embedded
	private AuditSection auditSection = new AuditSection();
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_LOGIN")
	private Date lastLogin;

	@Column(name="FAILED_LOGIN_ATTEMPTS")
    private Integer failedLoginAttempts = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="LOCK_TIME")
    private Date lockTime;

	@Column(name="RESET_PASSWORD_TOKEN")
    private String resetPasswordToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="RESET_PASSWORD_EXPIRY")
    private Date resetPasswordExpiry;
	
	@Embedded
	private CredentialsReset credentialsResetRequest;


	public CredentialsReset getCredentialsResetRequest() {
		return credentialsResetRequest;
	}

	public void setCredentialsResetRequest(CredentialsReset credentialsResetRequest) {
		this.credentialsResetRequest = credentialsResetRequest;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public AuditSection getAuditSection() {
		return auditSection;
	}

	@Override
	public void setAuditSection(AuditSection audit) {
		auditSection = audit;
		
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
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

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public Integer getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(Integer failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public Date getLockTime() {
		return lockTime;
	}

	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}

	public String getResetPasswordToken() {
		return resetPasswordToken;
	}

	public void setResetPasswordToken(String resetPasswordToken) {
		this.resetPasswordToken = resetPasswordToken;
	}

	public Date getResetPasswordExpiry() {
		return resetPasswordExpiry;
	}

	public void setResetPasswordExpiry(Date resetPasswordExpiry) {
		this.resetPasswordExpiry = resetPasswordExpiry;
	}
	

}
