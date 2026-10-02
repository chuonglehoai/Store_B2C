package com.salesmanager.core.business.services.catalog.product.availability;

import java.util.Objects;
import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.catalog.product.availability.ProductAvailabilityRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;

@Service("productAvailabilityService")
public class ProductAvailabilityServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductAvailability>
        implements ProductAvailabilityService {

    private ProductAvailabilityRepository productAvailabilityRepository;

    @Inject
    public ProductAvailabilityServiceImpl(ProductAvailabilityRepository productAvailabilityRepository) {
        super(productAvailabilityRepository);
        this.productAvailabilityRepository = productAvailabilityRepository;
    }

    @Override
    public ProductAvailability saveOrUpdate(ProductAvailability availability) throws ServiceException {
        if (isPositive(availability.getId())) {
            update(availability);
        } else {
            create(availability);
        }
        return availability;
    }

    private boolean isPositive(Long id) {
        return Objects.nonNull(id) && id > 0;
    }

    @Override
    public Optional<ProductAvailability> getByProductId(Long productId) {
        return productAvailabilityRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductAvailability> getByProductSku(String sku) {
        return productAvailabilityRepository.findByProductSku(sku);
    }
}