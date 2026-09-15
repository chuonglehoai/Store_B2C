package com.salesmanager.core.model.user;

import java.util.Set;

import com.salesmanager.core.model.common.Criteria;

public class PermissionCriteria extends Criteria {
	
	
	private String permissionName;
	
	private Set<Integer> groupIds;
	

	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public Set<Integer> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(Set<Integer> groupIds) {
		this.groupIds = groupIds;
	}


}
