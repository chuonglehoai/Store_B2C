package com.salesmanager.core.business.repositories.catalog.product.image;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.catalog.product.image.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Lấy toàn bộ ảnh của một sản phẩm
    @Query("SELECT i FROM ProductImage i " +
           "JOIN FETCH i.product p " +
           "WHERE p.id = :productId " +
           "ORDER BY i.sortOrder ASC")
    List<ProductImage> findByProductId(@Param("productId") Long productId);

    // Lấy một ảnh cụ thể của một sản phẩm
    @Query("SELECT i FROM ProductImage i " +
           "JOIN FETCH i.product p " +
           "WHERE i.id = :imageId AND p.id = :productId")
    Optional<ProductImage> findByIdAndProductId(@Param("imageId") Long imageId, @Param("productId") Long productId);
}