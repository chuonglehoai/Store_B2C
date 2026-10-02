package com.salesmanager.core.business.services.catalog.product.image;

import java.util.List;
import java.util.Optional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.file.ProductImageSize;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.content.ImageContentFile;
import com.salesmanager.core.model.content.OutputContentFile;

public interface ProductImageService extends SalesManagerEntityService<Long, ProductImage> {

    void addProductImage(Product product, ProductImage productImage, ImageContentFile inputImage) throws ServiceException;

    OutputContentFile getProductImage(ProductImage productImage, ProductImageSize size) throws ServiceException;

    List<OutputContentFile> getProductImages(Product product) throws ServiceException;
    
    Optional<ProductImage> getProductImage(Long imageId, Long productId);

    void removeProductImage(ProductImage productImage) throws ServiceException;

    ProductImage saveOrUpdate(ProductImage productImage) throws ServiceException;

    OutputContentFile getProductImage(String productCode, String fileName, final ProductImageSize size) throws ServiceException;

    void addProductImages(Product product, List<ProductImage> productImages) throws ServiceException;
    
    void updateProductImage(Product product, ProductImage productImage);
}