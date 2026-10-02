package com.salesmanager.core.model.catalog.product.image;

import java.io.InputStream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@Table(name = "PRODUCT_IMAGE")
public class ProductImage extends SalesManagerEntity<Long, ProductImage> {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "PRODUCT_IMAGE_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "PRODUCT_IMAGE")
	private String productImage;
	
	@Column(name = "DEFAULT_IMAGE")
	private boolean defaultImage = true;
	
	@ManyToOne(targetEntity = Product.class)
	@JoinColumn(name = "PRODUCT_ID", nullable = false)
	private Product product;
	
	@Column(name = "SORT_ORDER")
	private Integer sortOrder = 0;
	
	@Transient
	private InputStream image = null;

	public ProductImage(){}

	public String getProductImage() { return productImage; }
	public void setProductImage(String productImage) { this.productImage = productImage; }

	public boolean isDefaultImage() { return defaultImage; }
	public void setDefaultImage(boolean defaultImage) { this.defaultImage = defaultImage; }
	
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

	@Override
	public Long getId() { return id; }
	@Override
	public void setId(Long id) { this.id = id; }

	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }

	public InputStream getImage() { return image; }
	public void setImage(InputStream image) { this.image = image; }
}