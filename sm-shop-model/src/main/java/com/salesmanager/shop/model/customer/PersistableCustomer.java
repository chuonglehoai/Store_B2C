package com.salesmanager.shop.model.customer;

import java.util.List;
import com.salesmanager.shop.model.security.PersistableGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
	


@ApiModel(value="Customer", description="Customer model object")
public class PersistableCustomer extends CustomerEntity {

	/**
	 * 
	 */
    @ApiModelProperty(notes = "Customer password")
	@NotBlank(message = "Mật khẩu không được để trống")
	private String password;
    private String repeatPassword = null;
	private static final long serialVersionUID = 1L;
	private List<PersistableGroup> groups;
	

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<PersistableGroup> getGroups() {
		return groups;
	}
	public void setGroups(List<PersistableGroup> groups) {
		this.groups = groups;
	}
	public String getRepeatPassword() {
		return repeatPassword;
	}
	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}
	

}
