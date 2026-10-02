package com.salesmanager.shop.facade.product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.shop.model.product.PersistableProduct;
import com.salesmanager.shop.model.product.ReadableProduct;
import com.salesmanager.shop.model.product.ReadableProductList;
import com.salesmanager.shop.model.product.ReadableImage;

public interface ProductFacade {

    // 1. Lưu hoặc Cập nhật sản phẩm từ API (Giao diện Admin)
    ReadableProduct saveProduct(PersistableProduct product);

    // 2. Lấy chi tiết sản phẩm theo mã SKU
    ReadableProduct getProductBySku(String sku);

    // 3. Lấy chi tiết sản phẩm theo ID
    ReadableProduct getProductById(Long id);

    // 4. Lấy danh sách sản phẩm (có phân trang và tìm kiếm)
    ReadableProductList getProducts(String keyword, Pageable pageable);

    // 5. Xóa sản phẩm
    void deleteProduct(Long id);

    // 6. Nhập liệu hàng loạt từ file Excel
    void importProductsFromExcel(MultipartFile file);

    void addProductImages(Long productId, MultipartFile[] files);

    List<ReadableImage> getProductImages(Long productId);

    void removeProductImage(Long productId, Long imageId);
}