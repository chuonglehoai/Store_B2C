package com.salesmanager.core.business.services.catalog.product.image;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.jsoup.helper.Validate;

import com.salesmanager.core.business.configuration.events.products.DeleteProductImageEvent;
import com.salesmanager.core.business.configuration.events.products.SaveProductImageEvent;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.modules.cms.product.ProductFileManager;
import com.salesmanager.core.business.repositories.catalog.product.image.ProductImageRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.file.ProductImageSize;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.ImageContentFile;
import com.salesmanager.core.model.content.OutputContentFile;

@Service("productImage")
public class ProductImageServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductImage>
        implements ProductImageService {

    private ProductImageRepository productImageRepository;

    @Inject
    public ProductImageServiceImpl(ProductImageRepository productImageRepository) {
        super(productImageRepository);
        this.productImageRepository = productImageRepository;
    }

    @Inject
    private ProductFileManager productFileManager;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public ProductImage getById(Long id) {
        return productImageRepository.findById(id).orElse(null);
    }

    @Override
    public void addProductImages(Product product, List<ProductImage> productImages) throws ServiceException {
        try {
            for (ProductImage productImage : productImages) {
                Assert.notNull(productImage.getImage(), "Image must not be null");

                InputStream inputStream = productImage.getImage();
                ImageContentFile cmsContentImage = new ImageContentFile();
                cmsContentImage.setFileName(productImage.getProductImage());
                cmsContentImage.setFile(inputStream);
                cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                addProductImage(product, productImage, cmsContentImage);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void addProductImage(Product product, ProductImage productImage, ImageContentFile inputImage)
            throws ServiceException {

        productImage.setProduct(product);

        try {
            Assert.notNull(inputImage.getFile(), "ImageContentFile.file cannot be null");
            productFileManager.addProductImage(productImage, inputImage);

            ProductImage img = saveOrUpdate(productImage);
            eventPublisher.publishEvent(new SaveProductImageEvent(eventPublisher, img, product));
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            try {
                if (inputImage.getFile() != null) {
                    inputImage.getFile().close();
                }
            } catch (Exception ignore) {}
        }
    }

    @Override
    public ProductImage saveOrUpdate(ProductImage productImage) throws ServiceException {
        return productImageRepository.save(productImage);
    }

    @Override
    public OutputContentFile getProductImage(ProductImage productImage, ProductImageSize size) throws ServiceException {
        
        ProductImage pi = new ProductImage();
        pi.setProductImage(productImage.getProductImage());
        pi.setProduct(productImage.getProduct());

        return productFileManager.getProductImage(pi);
    }

    @Override
    public OutputContentFile getProductImage(final String productCode, final String fileName,
            final ProductImageSize size) throws ServiceException {
        return productFileManager.getProductImage( productCode, fileName, size);
    }

    @Override
    public List<OutputContentFile> getProductImages(Product product) throws ServiceException {
        return productFileManager.getImages(product);
    }

    @Override
    public void removeProductImage(ProductImage productImage) throws ServiceException {
        if (!StringUtils.isBlank(productImage.getProductImage())) {
            productFileManager.removeProductImage(productImage);
        }
        ProductImage p = getById(productImage.getId());
        Product product = p.getProduct();
        
        delete(p);
        eventPublisher.publishEvent(new DeleteProductImageEvent(eventPublisher, p, product));
    }

    @Override
    public Optional<ProductImage> getProductImage(Long imageId, Long productId) {
        return productImageRepository.findByIdAndProductId(imageId, productId);
    }

    @Override
    public void updateProductImage(Product product, ProductImage productImage) {
        Validate.notNull(product, "Product cannot be null");
        Validate.notNull(productImage, "ProductImage cannot be null");
        productImage.setProduct(product);
        productImageRepository.save(productImage);
    }
}