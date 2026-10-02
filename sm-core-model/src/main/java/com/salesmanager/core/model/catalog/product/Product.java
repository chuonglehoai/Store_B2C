package com.salesmanager.core.model.catalog.product;

import java.util.Date;
import java.util.HashSet;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.common.audit.AuditListener;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT")
public class Product extends SalesManagerEntity<Long, Product> implements Auditable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_ID", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SKU", nullable = false, unique = true)
    private String sku;

    @Column(name = "AVAILABLE")
    private boolean available = true;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_AVAILABLE")
    private Date dateAvailable = new Date();

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;
    
    @Column(name="NAME", nullable = false, length=120)
    private String name; 
    
    @Column(name="DESCRIPTION", columnDefinition = "TEXT")
    private String description; 
    
    @Column(name = "PRODUCT_HIGHLIGHT")
    private String productHighlight; 

    @Column(name = "SEF_URL", length=120)
    private String seUrl; 

    // Quan hệ 1-Nhiều với ProductAvailability
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
    private Set<ProductAvailability> availabilities = new HashSet<ProductAvailability>();

    // Quan hệ 1-Nhiều với ProductImage
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product")
    private Set<ProductImage> images = new HashSet<ProductImage>();

    // ĐÃ SỬA LỖI JPA: Bỏ các thuộc tính updatable/insertable để Hibernate tự xử lý an toàn
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "PRODUCT_CATEGORY", 
        joinColumns = @JoinColumn(name = "PRODUCT_ID"), 
        inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID"))
    private Set<Category> categories = new HashSet<Category>();

    @Embedded
    private AuditSection auditSection = new AuditSection();

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductHighlight() { return productHighlight; }
    public void setProductHighlight(String productHighlight) { this.productHighlight = productHighlight; }

    public String getSeUrl() { return seUrl; }
    public void setSeUrl(String seUrl) { this.seUrl = seUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Date getDateAvailable() { return dateAvailable; }
    public void setDateAvailable(Date dateAvailable) { this.dateAvailable = dateAvailable; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Set<ProductAvailability> getAvailabilities() { return availabilities; }
    public void setAvailabilities(Set<ProductAvailability> availabilities) { this.availabilities = availabilities; }

    public Set<ProductImage> getImages() { return images; }
    public void setImages(Set<ProductImage> images) { this.images = images; }

    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }

    @Override
    public AuditSection getAuditSection() { return auditSection; }
    @Override
    public void setAuditSection(AuditSection auditSection) { this.auditSection = auditSection; }
}