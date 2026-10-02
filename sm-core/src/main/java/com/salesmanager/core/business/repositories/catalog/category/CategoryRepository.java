package com.salesmanager.core.business.repositories.catalog.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.catalog.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Lấy chi tiết Category có fetch cả parent
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.id = :id")
    Optional<Category> findByIdWithParent(@Param("id") Long id);

    @Query("SELECT c FROM Category c WHERE c.code = :code")
    Optional<Category> findByCode(@Param("code") String code);

    // Lọc theo tên (Hỗ trợ phân trang)
    @Query("SELECT c FROM Category c WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) ORDER BY c.sortOrder ASC")
    Page<Category> findByName(@Param("name") String name, Pageable pageable);

    // Lấy toàn bộ cây danh mục con (Cơ chế siêu hay của hệ thống cũ)
    @Query("SELECT c FROM Category c WHERE c.lineage LIKE %:lineage% ORDER BY c.lineage, c.sortOrder ASC")
    List<Category> findByLineage(@Param("lineage") String lineage);

    // Lấy danh mục gốc (Level 0)
    @Query("SELECT c FROM Category c WHERE c.depth = :depth ORDER BY c.lineage, c.sortOrder ASC")
    List<Category> findByDepth(@Param("depth") int depth);
}