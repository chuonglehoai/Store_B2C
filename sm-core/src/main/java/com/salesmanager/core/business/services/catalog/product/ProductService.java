package com.salesmanager.core.business.services.catalog.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.Product;

public interface ProductService extends SalesManagerEntityService<Long, Product> {

    Optional<Product> retrieveById(Long id);

    Product saveProduct(Product product) throws ServiceException;

    boolean exists(String sku);

    Page<Product> searchProducts(String keyword, Pageable pageable);

    Product getBySku(String productCode) throws ServiceException;
    
    Optional<Product> getBySkuWithInventoryDetails(String sku);

    List<Product> getProducts(List<Long> categoryIds) throws ServiceException;
}