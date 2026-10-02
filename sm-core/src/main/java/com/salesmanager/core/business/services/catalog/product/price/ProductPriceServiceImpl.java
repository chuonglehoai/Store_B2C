package com.salesmanager.core.business.services.catalog.product.price;

import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.catalog.product.price.ProductPriceRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;

@Service("productPrice")
public class ProductPriceServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductPrice> 
    implements ProductPriceService {
    
    private ProductPriceRepository productPriceRepository;

    @Inject
    public ProductPriceServiceImpl(ProductPriceRepository productPriceRepository) {
        super(productPriceRepository);
        this.productPriceRepository = productPriceRepository;
    }

    @Override
    public ProductPrice saveOrUpdate(ProductPrice price) throws ServiceException {
        return productPriceRepository.save(price);
    }
    
    @Override
    public void delete(ProductPrice price) throws ServiceException {
        price = this.getById(price.getId());
        super.delete(price);
    }

}