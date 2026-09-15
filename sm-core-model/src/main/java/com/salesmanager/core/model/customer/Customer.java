package com.salesmanager.core.model.customer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.salesmanager.core.model.catalog.product.review.ProductReview;
import com.salesmanager.core.model.common.CredentialsReset;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.utils.CloneUtils;

@Entity
@Table(name = "CUSTOMER", 
	 uniqueConstraints=
			@UniqueConstraint(columnNames = {"CUSTOMER_NICK"}))
public class Customer extends SalesManagerEntity<Long, Customer> implements Auditable {
	private static final long serialVersionUID = 1L;
	
	@Id
    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NotBlank(message = "Tên tài khoản không được để trống")
	@Column(name = "CUSTOMER_FULL_NAME", length = 100)
	private String fullName;

	@Column(name = "CUSTOMER_USERNAME", length = 100)
	private String userName;

	@JsonIgnore
	@Embedded
	private AuditSection auditSection = new AuditSection();
	
	@Column(name="CUSTOMER_GENDER", length=1, nullable=true)
	@Enumerated(value = EnumType.STRING)
	private CustomerGender gender;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CUSTOMER_DOB")
	private Date dateOfBirth;
	
	@NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
	@NotEmpty
	@Column(name="CUSTOMER_EMAIL_ADDRESS", length=96, nullable=false)
	private String emailAddress;
	
	@NotBlank(message = "Tên đăng nhập (nick) không được để trống")
	@Column(name="CUSTOMER_NICK", length=96)
	private String nick;

	@NotBlank(message = "Mật khẩu không được để trống")
	@JsonIgnore
	@Column(name="CUSTOMER_PASSWORD", length=60)
	private String password;

	/*@OneToMany(mappedBy = "customer", targetEntity = ProductReview.class)
	private List<ProductReview> reviews = new ArrayList<ProductReview>();*/

	@Column(name = "CUSTOMER_AVATAR")
	private String avatarUrl;

	@Column(name = "CUSTOMER_ADDRESS", length = 255)
	private String address;
	@Column(name="CUSTOMER_TELEPHONE", length=96)
	private String telephone;

	@Column(name = "ACTIVE")
	private boolean active = true;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
	@JoinTable(
		name = "CUSTOMER_GROUP",
		joinColumns = @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "CUSTOMER_ID"),
		inverseJoinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID")
	)
	private List<Group> groups = new ArrayList<>();

	@Embedded
	private CredentialsReset credentialsResetRequest = null;

	public Customer() {
	}

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getDateOfBirth() {
		return CloneUtils.clone(dateOfBirth);
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = CloneUtils.clone(dateOfBirth);
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*public List<ProductReview> getReviews() {
		return reviews;
	}

	public void setReviews(List<ProductReview> reviews) {
		this.reviews = reviews;
	}*/

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

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGender(CustomerGender gender) {
		this.gender = gender;
	}

	public CustomerGender getGender() {
		return gender;
	}

	@Override
	public AuditSection getAuditSection() {
		return auditSection;
	}

	@Override
	public void setAuditSection(AuditSection auditSection) {
		this.auditSection = auditSection;
	}

	public CredentialsReset getCredentialsResetRequest() {
		return credentialsResetRequest;
	}

	public void setCredentialsResetRequest(CredentialsReset credentialsResetRequest) {
		this.credentialsResetRequest = credentialsResetRequest;
	}
	
}
