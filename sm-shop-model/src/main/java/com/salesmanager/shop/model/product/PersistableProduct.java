package com.salesmanager.shop.model.product;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO dùng để nhận dữ liệu từ Client (Form Thêm mới, Cập nhật hoặc từ file Excel)
 */
public class PersistableProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;
    private String description;
    private String productHighlight; // Mô tả ngắn
    private String categoryCode;     // Để tìm danh mục khi lưu
    private int quantity;            // Tồn kho cơ bản
    private BigDecimal price;        // Giá bán cơ bản
    private boolean available = true;
    private Integer sortOrder = 0;

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductHighlight() { return productHighlight; }
    public void setProductHighlight(String productHighlight) { this.productHighlight = productHighlight; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}