package com.salesmanager.core.business.repositories.catalog.product.availability;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;

@Repository
public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailability, Long> {

    // 1. Lấy thông tin Tồn kho và Giá theo ID sản phẩm
    @Query("SELECT DISTINCT a FROM ProductAvailability a " +
           "LEFT JOIN FETCH a.prices p " +
           "JOIN FETCH a.product pr " +
           "WHERE pr.id = :productId")
    Optional<ProductAvailability> findByProductId(@Param("productId") Long productId);

    // 2. Lấy thông tin Tồn kho và Giá theo SKU sản phẩm
    @Query("SELECT DISTINCT a FROM ProductAvailability a " +
           "LEFT JOIN FETCH a.prices p " +
           "JOIN FETCH a.product pr " +
           "WHERE pr.sku = :sku")
    Optional<ProductAvailability> findByProductSku(@Param("sku") String sku);
}