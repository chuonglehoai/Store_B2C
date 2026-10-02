package com.salesmanager.core.business.services.catalog.product.availability;

import java.util.Optional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;

public interface ProductAvailabilityService extends SalesManagerEntityService<Long, ProductAvailability> {

    ProductAvailability saveOrUpdate(ProductAvailability availability) throws ServiceException;

    Optional<ProductAvailability> getByProductId(Long productId);
    
    Optional<ProductAvailability> getByProductSku(String sku);
}