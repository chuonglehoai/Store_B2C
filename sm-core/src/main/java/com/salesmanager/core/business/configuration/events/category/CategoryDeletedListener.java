package com.salesmanager.core.business.configuration.events.category;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.model.catalog.product.Product;

@Component
public class CategoryDeletedListener {

    @Autowired
    private ProductService productService;

    // Annotation này giúp Spring tự động vểnh tai nghe mỗi khi có CategoryDeletedEvent
    @EventListener
    @Transactional
    public void onCategoryDeleted(CategoryDeletedEvent event) {
        Long deletedCategoryId = event.getCategory().getId();
        
        try {
            // Lấy các sản phẩm thuộc danh mục vừa bị xóa
            List<Product> products = productService.getProducts(List.of(deletedCategoryId));
            
            for (Product product : products) {
                // Gỡ danh mục đó ra khỏi sản phẩm
                product.getCategories().removeIf(c -> c.getId().equals(deletedCategoryId));
                
                if (product.getCategories().isEmpty()) {
                    // Nếu sản phẩm mồ côi (không còn danh mục nào), xóa luôn sản phẩm
                    productService.delete(product);
                } else {
                    // Nếu vẫn còn danh mục khác, chỉ cập nhật lại sản phẩm
                    productService.saveProduct(product);
                }
            }
        } catch (Exception e) {
            // Log lỗi nếu có
            throw new RuntimeException("Lỗi khi dọn dẹp sản phẩm sau khi xóa danh mục", e);
        }
    }
}