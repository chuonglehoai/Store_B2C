package com.salesmanager.core.model.catalog.product;

import java.util.List;

import com.salesmanager.core.model.common.Criteria;

public class ProductCriteria extends Criteria {
	
	public static final String ORIGIN_SHOP = "shop";
	public static final String ORIGIN_ADMIN = "admin";
	
	private String productName;
	private String origin = ORIGIN_SHOP;
	private Boolean available = null;
	private List<Long> categoryIds;
	private List<Long> productIds;
	private String sku;
	private String status;

	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }

	public List<Long> getCategoryIds() { return categoryIds; }
	public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }

	public Boolean getAvailable() { return available; }
	public void setAvailable(Boolean available) { this.available = available; }

	public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
	public List<Long> getProductIds() { return productIds; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getOrigin() { return origin; }
	public void setOrigin(String origin) { this.origin = origin; }

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
}