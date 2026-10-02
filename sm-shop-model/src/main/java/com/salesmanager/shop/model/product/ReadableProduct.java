package com.salesmanager.shop.model.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO dùng để trả dữ liệu Sản phẩm từ Server về cho Frontend
 */
public class ReadableProduct implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String categoryName;
    private int quantity;
    private BigDecimal price;
    private boolean available;
    private Integer sortOrder;
    
    // Tạm giữ lại danh sách ảnh gốc dạng chuỗi (URL/tên file) để giao diện hiển thị
    private List<String> imageUrls = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}