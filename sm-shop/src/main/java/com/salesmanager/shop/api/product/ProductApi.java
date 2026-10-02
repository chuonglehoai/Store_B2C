package com.salesmanager.shop.api.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.shop.model.product.PersistableProduct;
import com.salesmanager.shop.model.product.ReadableProduct;
import com.salesmanager.shop.model.product.ReadableProductList;
import com.salesmanager.shop.facade.product.ProductFacade;
import com.salesmanager.shop.model.product.ReadableImage;

/**
 * REST API chuyên quản lý Sản phẩm (Thêm, Sửa, Xóa, Lấy danh sách, Import Excel)
 * Cho dự án B2C Độc lập.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductApi {

    @Autowired
    private ProductFacade productFacade;

    // TẠO MỚI HOẶC CẬP NHẬT SẢN PHẨM (TỪ FORM)
    @PostMapping
    public ResponseEntity<ReadableProduct> saveProduct(@RequestBody PersistableProduct product) {
        ReadableProduct savedProduct = productFacade.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    // LẤY DANH SÁCH SẢN PHẨM (PHÂN TRANG & TÌM KIẾM)
    @GetMapping
    public ResponseEntity<ReadableProductList> getProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        ReadableProductList productList = productFacade.getProducts(keyword, pageable);
        return new ResponseEntity<>(productList, HttpStatus.OK);
    }

    // LẤY CHI TIẾT SẢN PHẨM (THEO SKU)
    @GetMapping("/{sku}")
    public ResponseEntity<ReadableProduct> getProductBySku(@PathVariable String sku) {
        ReadableProduct product = productFacade.getProductBySku(sku);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    //  XÓA SẢN PHẨM (THEO ID)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productFacade.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 5. IMPORT SẢN PHẨM TỪ FILE EXCEL
    @PostMapping("/import")
    public ResponseEntity<String> importProductsFromExcel(@RequestParam("file") MultipartFile file) {
        // Kiểm tra sơ bộ loại file
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            return new ResponseEntity<>("Vui lòng tải lên file Excel (.xlsx) hợp lệ", HttpStatus.BAD_REQUEST);
        }

        productFacade.importProductsFromExcel(file);
        return new ResponseEntity<>("Import dữ liệu thành công", HttpStatus.OK);
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<String> uploadProductImages(
            @PathVariable Long id, 
            @RequestParam("file") MultipartFile[] files) {
         productFacade.addProductImages(id, files);
        
        return new ResponseEntity<>("Tải ảnh thành công", HttpStatus.CREATED);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ReadableImage>> getProductImages(@PathVariable Long id) {
        
        List<ReadableImage> images = productFacade.getProductImages(id);
        return new ResponseEntity<>(images, HttpStatus.OK);
        
    }
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Long id, 
            @PathVariable Long imageId) {
        
        productFacade.removeProductImage(id, imageId);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}