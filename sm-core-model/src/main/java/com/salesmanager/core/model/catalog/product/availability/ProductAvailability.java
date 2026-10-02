package com.salesmanager.core.model.catalog.product.availability;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.utils.CloneUtils;

@Entity
@Table(name = "PRODUCT_AVAILABILITY",
indexes = 
	{ 
		@Index(name="PRD_AVAIL_PRD_IDX", columnList = "PRODUCT_ID")
	}
)
public class ProductAvailability extends SalesManagerEntity<Long, ProductAvailability> implements Auditable {

	private static final long serialVersionUID = 1L;

	@Embedded
	private AuditSection auditSection = new AuditSection();

	@Id
	@Column(name = "PRODUCT_AVAIL_ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(targetEntity = Product.class)
	@JoinColumn(name = "PRODUCT_ID", nullable = false)
	private Product product;

	@NotNull
	@Column(name = "QUANTITY")
	private Integer productQuantity = 0;

	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_AVAILABLE")
	private Date productDateAvailable;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "PRODUCT_AVAIL_ID")
    private Set<ProductPrice> prices = new HashSet<ProductPrice>();
	
	@Transient
	public ProductPrice defaultPrice() {
		for (ProductPrice price : prices) {
			if (price.isDefaultPrice()) {
				return price;
			}
		}
		return new ProductPrice();
	}

	public ProductAvailability() {
	}

	public ProductAvailability(Product product) {
		this.product = product;
	}

	public Integer getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}

	public Date getProductDateAvailable() {
		return CloneUtils.clone(productDateAvailable);
	}

	public void setProductDateAvailable(Date productDateAvailable) {
		this.productDateAvailable = CloneUtils.clone(productDateAvailable);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Set<ProductPrice> getPrices() {
		return prices;
	}

	public void setPrices(Set<ProductPrice> prices) {
		this.prices = prices;
	}

	@Override
	public AuditSection getAuditSection() {
		return auditSection;
	}

	@Override
	public void setAuditSection(AuditSection audit) {
		this.auditSection = audit;
	}
}