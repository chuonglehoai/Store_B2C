package com.salesmanager.core.model.catalog.category;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotEmpty;

import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@EntityListeners(value = com.salesmanager.core.model.common.audit.AuditListener.class)
@Table(name = "CATEGORY",
    indexes = @Index(columnList = "LINEAGE"),
    uniqueConstraints = @UniqueConstraint(columnNames = { "CODE"}) )
public class Category extends SalesManagerEntity<Long, Category> implements Auditable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "CATEGORY_ID", unique=true, nullable=false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    // Mã định danh cực kỳ quan trọng dùng để map với file Excel
    @NotEmpty
    @Column(name="CODE", length=100, nullable=false)
    private String code;

    // Tên hiển thị trên giao diện
    @Column(name="NAME", nullable = false, length=120)
    private String name; 
    
    // --- CẤU TRÚC CÂY DANH MỤC ĐA CẤP ---
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<Category> categories = new ArrayList<>();

    @Column(name = "DEPTH")
    private Integer depth;

    @Column(name = "LINEAGE")
    private String lineage;
    // ------------------------------------

    // Thứ tự sắp xếp trên Menu
    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    // Ẩn/Hiện danh mục
    @Column(name = "VISIBLE")
    private boolean visible = true;


    public Category() {}

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public Long getId() { return this.id; }
    @Override
    public void setId(Long id) { this.id = id; }
    
    @Override
    public AuditSection getAuditSection() { return auditSection; }
    @Override
    public void setAuditSection(AuditSection auditSection) { this.auditSection = auditSection; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public Integer getDepth() { return depth; }
    public void setDepth(Integer depth) { this.depth = depth; }

    public String getLineage() { return lineage; }
    public void setLineage(String lineage) { this.lineage = lineage; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
}