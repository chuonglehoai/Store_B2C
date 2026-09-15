package com.salesmanager.shop.model.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.salesmanager.shop.model.security.ReadableGroup;


public class ReadableCustomer extends CustomerEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ReadableGroup> groups = new ArrayList<ReadableGroup>();
	
	public List<ReadableGroup> getGroups() {
		return groups;
	}
	public void setGroups(List<ReadableGroup> groups) {
		this.groups = groups;
	}

}
