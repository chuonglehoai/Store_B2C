package com.salesmanager.core.business.repositories.catalog.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.catalog.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 1. Kiểm tra tồn tại bằng SKU (Dùng khi Import Excel)
	boolean existsBySku(String sku);

	// 2. Tìm một sản phẩm theo SKU (Có Fetch sẵn Danh mục để hiển thị/Update)
	@Query("SELECT p FROM Product p " +
		   "LEFT JOIN FETCH p.categories c " +
		   "WHERE p.sku = :sku")
	Optional<Product> findBySku(@Param("sku") String sku);

	// 3. Tìm sản phẩm theo SKU kèm chi tiết (Kho & Giá) để chuẩn bị cho Import Cập nhật
	@Query("SELECT DISTINCT p FROM Product p " +
		   "LEFT JOIN FETCH p.availabilities a " +
		   "LEFT JOIN FETCH a.prices pr " +
		   "WHERE p.sku = :sku")
	Optional<Product> findBySkuWithInventoryDetails(@Param("sku") String sku);

	// 4. Lấy danh sách sản phẩm (Dùng ở trang Admin Quản lý sản phẩm)
	@Query("SELECT p FROM Product p " +
		   "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE c.id IN :categoryIds")
    List<Product> findByCategories(@Param("categoryIds") List<Long> categoryIds);
}