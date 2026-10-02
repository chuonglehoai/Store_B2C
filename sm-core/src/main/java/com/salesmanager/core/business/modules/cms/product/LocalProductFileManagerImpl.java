package com.salesmanager.core.business.modules.cms.product;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.file.ProductImageSize;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.content.ImageContentFile;
import com.salesmanager.core.model.content.OutputContentFile;

@Component
@Primary // Đánh dấu đây là Bean chính duy nhất xử lý file ảnh
public class LocalProductFileManagerImpl extends ProductFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProductFileManagerImpl.class);
    private static final String ROOT_PATH = "uploads/products/";

    @Override
    public void addProductImage(ProductImage productImage, ImageContentFile contentImage) throws ServiceException {
        try {
            String sku = productImage.getProduct().getSku();
            Path dirPath = Paths.get(ROOT_PATH + sku);
            
            if (Files.notExists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(contentImage.getFileName());
            try (InputStream isFile = contentImage.getFile()) {
                Files.copy(isFile, filePath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Đã lưu ảnh thành công tại: " + filePath.toAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Lỗi khi lưu ảnh sản phẩm: ", e);
            throw new ServiceException("Không thể lưu ảnh sản phẩm", e);
        }
    }

    @Override
    public void removeProductImage(ProductImage productImage) throws ServiceException {
        try {
            String sku = productImage.getProduct().getSku();
            Path filePath = Paths.get(ROOT_PATH + sku + "/" + productImage.getProductImage());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new ServiceException("Không thể xóa ảnh", e);
        }
    }

    @Override
    public void removeProductImages(Product product) throws ServiceException {
        try {
            Path dirPath = Paths.get(ROOT_PATH + product.getSku());
            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                     .filter(Files::isRegularFile)
                     .map(Path::toFile)
                     .forEach(java.io.File::delete);
                Files.deleteIfExists(dirPath);
            }
        } catch (Exception e) {
            throw new ServiceException("Không thể xóa thư mục ảnh của sản phẩm", e);
        }
    }

    // Các hàm Get trả về rỗng vì ta dùng WebStaticConfig để cho Frontend đọc trực tiếp URL
    @Override
    public OutputContentFile getProductImage(ProductImage productImage) throws ServiceException { return null; }

    @Override
    public List<OutputContentFile> getImages(Product product) throws ServiceException { return Collections.emptyList(); }
    
    // (Implement các phương thức trống nếu Interface yêu cầu thêm)
    @Override
    public OutputContentFile getProductImage(String productCode, String imageName) throws ServiceException { return null; }
    
    @Override
    public OutputContentFile getProductImage(String productCode, String imageName, ProductImageSize size) throws ServiceException { return null; }
}