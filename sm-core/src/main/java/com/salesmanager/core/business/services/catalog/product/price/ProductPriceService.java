package com.salesmanager.core.business.services.catalog.product.price;

import java.util.Optional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;

public interface ProductPriceService extends SalesManagerEntityService<Long, ProductPrice> {

    ProductPrice saveOrUpdate(ProductPrice price) throws ServiceException;
    
}