package com.salesmanager.core.business.modules.cms.product;

import java.util.List;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.file.ProductImageSize;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.content.OutputContentFile;

public interface ProductImageGet {

  OutputContentFile getProductImage(final String productCode, final String imageName) throws ServiceException;

  OutputContentFile getProductImage(final String productCode, final String imageName, final ProductImageSize size) throws ServiceException;

  OutputContentFile getProductImage(ProductImage productImage) throws ServiceException;

  List<OutputContentFile> getImages(Product product) throws ServiceException;
}