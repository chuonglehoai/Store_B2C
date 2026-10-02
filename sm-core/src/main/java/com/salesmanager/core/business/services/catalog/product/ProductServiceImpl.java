package com.salesmanager.core.business.services.catalog.product;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.catalog.product.ProductRepository;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.product.availability.ProductAvailabilityService;
import com.salesmanager.core.business.services.catalog.product.image.ProductImageService;
import com.salesmanager.core.business.services.catalog.product.price.ProductPriceService;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.ImageContentFile;

@Service("productService")
public class ProductServiceImpl extends SalesManagerEntityServiceImpl<Long, Product> implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    ProductRepository productRepository;

    @Inject
    CategoryService categoryService;

    @Inject
    ProductAvailabilityService productAvailabilityService;

    @Inject
    ProductPriceService productPriceService;

    @Inject
    ProductImageService productImageService;

    @Inject
    public ProductServiceImpl(ProductRepository productRepository) {
        super(productRepository);
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> retrieveById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void delete(Product product) throws ServiceException {
        Validate.notNull(product, "Product cannot be null");
        
        // Đảm bảo không bị lỗi entity detached
        product = this.getById(product.getId());

        product.setCategories(null);

        // Giữ nguyên logic Xóa ảnh sạch sẽ khỏi ổ cứng
        Set<ProductImage> images = product.getImages();
        for (ProductImage image : images) {
            productImageService.removeProductImage(image);
        }
        product.setImages(null);

        super.delete(product);
    }

    @Override
    public Product saveProduct(Product product) throws ServiceException {
        Validate.notNull(product, "product cannot be null");
        Validate.notNull(product.getAvailabilities(), "product must have at least one availability");
        Validate.notEmpty(product.getAvailabilities(), "product must have at least one availability");

        // Giữ lại logic lưu và chép ảnh
        Set<ProductImage> originalProductImages = new HashSet<ProductImage>(product.getImages());

        if (product.getId() != null && product.getId() > 0) {
            super.update(product);
        } else {
            super.create(product);
        }

        List<Long> newImageIds = new ArrayList<Long>();
        Set<ProductImage> images = product.getImages();

        try {
            if (images != null && images.size() > 0) {
                for (ProductImage image : images) {
                    if (image.getImage() != null && (image.getId() == null || image.getId() == 0L)) {
                        image.setProduct(product);

                        InputStream inputStream = image.getImage();
                        ImageContentFile cmsContentImage = new ImageContentFile();
                        cmsContentImage.setFileName(image.getProductImage());
                        cmsContentImage.setFile(inputStream);
                        cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                        productImageService.addProductImage(product, image, cmsContentImage);
                        newImageIds.add(image.getId());
                    } else {
                        if (image.getId() != null) {
                            productImageService.saveOrUpdate(image);
                            newImageIds.add(image.getId());
                        }
                    }
                }
            }

            if (originalProductImages != null) {
                for (ProductImage image : originalProductImages) {
                    if (image.getImage() != null && image.getId() == null) {
                        image.setProduct(product);

                        InputStream inputStream = image.getImage();
                        ImageContentFile cmsContentImage = new ImageContentFile();
                        cmsContentImage.setFileName(image.getProductImage());
                        cmsContentImage.setFile(inputStream);
                        cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                        productImageService.addProductImage(product, image, cmsContentImage);
                        newImageIds.add(image.getId());
                    } else {
                        if (!newImageIds.contains(image.getId())) {
                            productImageService.delete(image);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Cannot save images " + e.getMessage());
        }

        return product;
    }

    @Override
    public boolean exists(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public Product getBySku(String productCode) throws ServiceException {
        return productRepository.findBySku(productCode)
                .orElseThrow(() -> new ServiceException("Cannot get product with sku [" + productCode + "]"));
    }
    
    @Override
    public Optional<Product> getBySkuWithInventoryDetails(String sku) {
        return productRepository.findBySkuWithInventoryDetails(sku);
    }

    @Override
    public List<Product> getProducts(List<Long> categoryIds) throws ServiceException {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findByCategories(categoryIds);
    }
}