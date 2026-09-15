package com.salesmanager.shop.model.user;

import java.util.ArrayList;
import java.util.List;
import com.salesmanager.shop.model.security.ReadableGroup;
import com.salesmanager.shop.model.security.ReadablePermission;

public class ReadableUser extends UserEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String lastLogin;

	private List<ReadablePermission> permissions = new ArrayList<ReadablePermission>();
	private List<ReadableGroup> groups = new ArrayList<ReadableGroup>();

	public List<ReadableGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<ReadableGroup> groups) {
		this.groups = groups;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

	public List<ReadablePermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<ReadablePermission> permissions) {
		this.permissions = permissions;
	}

}
