package com.salesmanager.shop.facade.product;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.availability.ProductAvailabilityService;
import com.salesmanager.core.business.services.catalog.product.price.ProductPriceService;
import com.salesmanager.core.business.services.catalog.product.image.ProductImageService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.shop.model.product.PersistableProduct;
import com.salesmanager.shop.model.product.ReadableProduct;
import com.salesmanager.shop.model.product.ReadableProductList;
import com.salesmanager.shop.model.product.ReadableImage;
import com.salesmanager.shop.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.api.exception.ServiceRuntimeException;

@Service("productFacade")
public class ProductFacadeImpl implements ProductFacade {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductAvailabilityService productAvailabilityService;

    @Autowired
    private ProductPriceService productPriceService;

    @Autowired
    private ProductImageService productImageService;

    @Override
    @Transactional
    public ReadableProduct saveProduct(PersistableProduct persistableProduct) {
        Assert.notNull(persistableProduct, "Product data cannot be null");
        Assert.notNull(persistableProduct.getSku(), "Product SKU cannot be null");

        try {
            // 1. Lấy SP cũ hoặc tạo mới nếu chưa có
            Product product = productService.getBySkuWithInventoryDetails(persistableProduct.getSku())
                    .orElse(new Product());

            // 2. Chuyển đổi dữ liệu DTO -> Entity
            mapToEntity(persistableProduct, product);

            // 3. Xử lý Danh mục
            product.getCategories().clear();
            if (persistableProduct.getCategoryCode() != null) {
                Category category = categoryService.getByCode(persistableProduct.getCategoryCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + persistableProduct.getCategoryCode()));
                product.getCategories().add(category);
            }

            // 4. Xử lý Tồn kho & Giá
            ProductAvailability availability = productAvailabilityService.getByProductSku(product.getSku())
                    .orElse(new ProductAvailability());
            availability.setProduct(product);
            availability.setProductQuantity(persistableProduct.getQuantity());

            ProductPrice price = new ProductPrice(); 
            if (availability.getPrices() != null && !availability.getPrices().isEmpty()) {
                price = availability.getPrices().iterator().next();
            }
            price.setProductPriceAmount(persistableProduct.getPrice());
            price.setDefaultPrice(true);

            // Gắn Giá vào Kho
            if(availability.getPrices() != null) {
                availability.getPrices().clear();
                availability.getPrices().add(price);
            }

            // Gắn Kho vào Sản phẩm
            if(product.getAvailabilities() != null) {
                product.getAvailabilities().clear();
                product.getAvailabilities().add(availability);
            }

            // 5. Lưu toàn bộ (Cascade sẽ lo việc lưu các entity con)
            productService.saveProduct(product);

            return getProductBySku(product.getSku());

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while saving product", e);
        }
    }

    @Override
    public ReadableProduct getProductBySku(String sku) {
        Product product = productService.getBySkuWithInventoryDetails(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToReadable(product);
    }

    @Override
    public ReadableProduct getProductById(Long id) {
        Product product = productService.retrieveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        // Fetch lại kèm kho & giá thông qua SKU để map đầy đủ
        return getProductBySku(product.getSku()); 
    }

    @Override
    public ReadableProductList getProducts(String keyword, Pageable pageable) {
        Page<Product> productPage = productService.searchProducts(keyword, pageable);
        
        List<ReadableProduct> readableProducts = productPage.getContent().stream()
                .map(this::mapToReadable)
                .collect(Collectors.toList());

        ReadableProductList productList = new ReadableProductList();
        productList.setProducts(readableProducts);
        productList.setTotalPages(productPage.getTotalPages());
        productList.setRecordsTotal(productPage.getTotalElements());
        productList.setNumber(readableProducts.size());
        
        return productList;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        try {
            Product product = productService.retrieveById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
            productService.delete(product);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting product", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importProductsFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;

                PersistableProduct dto = new PersistableProduct();
                dto.setSku(row.getCell(0).getStringCellValue().trim());
                dto.setName(row.getCell(1).getStringCellValue().trim());
                dto.setCategoryCode(row.getCell(2).getStringCellValue().trim());
                dto.setQuantity((int) row.getCell(3).getNumericCellValue());
                dto.setPrice(BigDecimal.valueOf(row.getCell(4).getNumericCellValue()));
                
                if (row.getCell(5) != null) {
                    dto.setDescription(row.getCell(5).getStringCellValue().trim());
                }

                // Tận dụng lại chính hàm saveProduct ở trên để không phải viết lại code lưu
                this.saveProduct(dto);
            }

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error importing Excel file", e);
        }
    }

    // =====================================================================
    // CÁC HÀM HELPER CHUYỂN ĐỔI DỮ LIỆU (THAY THẾ POPULATOR/MAPPER CŨ)
    // =====================================================================

    private void mapToEntity(PersistableProduct source, Product target) {
        target.setSku(source.getSku());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setAvailable(source.isAvailable());
        target.setSortOrder(source.getSortOrder());
        target.setProductHighlight(source.getProductHighlight());

        if (target.getSeUrl() == null && source.getName() != null) {
            target.setSeUrl(source.getName().replaceAll("\\s+", "-").toLowerCase());
        }
    }

    private ReadableProduct mapToReadable(Product source) {
        ReadableProduct target = new ReadableProduct();
        target.setId(source.getId());
        target.setSku(source.getSku());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setAvailable(source.isAvailable());
        target.setSortOrder(source.getSortOrder());
        
        if (!source.getCategories().isEmpty()) {
            target.setCategoryName(source.getCategories().iterator().next().getCode());
        }

        // Lấy Tồn kho & Giá từ Set (Vì B2C của bạn chỉ có 1 kho mặc định)
        if (source.getAvailabilities() != null && !source.getAvailabilities().isEmpty()) {
            ProductAvailability availability = source.getAvailabilities().iterator().next();
            target.setQuantity(availability.getProductQuantity());

            if (availability.getPrices() != null && !availability.getPrices().isEmpty()) {
                ProductPrice price = availability.getPrices().iterator().next();
                target.setPrice(price.getProductPriceAmount());
            }
        }

        return target;
    }

    @Override
    @Transactional
    public void addProductImages(Long productId, MultipartFile[] files) {
        Product product = productService.retrieveById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + productId));

        try {
            boolean hasDefaultImage = product.getImages() != null && 
                                      product.getImages().stream().anyMatch(ProductImage::isDefaultImage);

            List<ProductImage> contentImagesList = new ArrayList<>();
            int sortOrder = product.getImages() != null ? product.getImages().size() : 0;

            for (MultipartFile multipartFile : files) {
                if (!multipartFile.isEmpty()) {
                    ProductImage productImage = new ProductImage();
                    
                    // Lấy luồng byte của ảnh
                    productImage.setImage(multipartFile.getInputStream());
                    productImage.setProductImage(multipartFile.getOriginalFilename());
                    productImage.setProduct(product);
                    productImage.setSortOrder(sortOrder++);

                    // Gắn cờ ảnh mặc định cho ảnh đầu tiên
                    if (!hasDefaultImage) {
                        productImage.setDefaultImage(true);
                        hasDefaultImage = true;
                    }
                    contentImagesList.add(productImage);
                }
            }

            if (!contentImagesList.isEmpty()) {
                // Gọi xuống Core Service, Core sẽ tự lưu vào Database và ghi file ra ổ cứng
                productImageService.addProductImages(product, contentImagesList);
            }

        } catch (Exception e) {
            throw new ServiceRuntimeException("Lỗi trong quá trình upload ảnh", e);
        }
    }

    @Override
    @Transactional
    public List<ReadableImage> getProductImages(Long productId) {
        Product product = productService.getById(productId); 
            
            if (product == null) {
                return new ArrayList<>();
            }

            // Mở kết nối LAZY để lấy danh sách ảnh
            if (product.getImages() == null || product.getImages().isEmpty()) {
                return new ArrayList<>();
            }

        return product.getImages().stream().map(img -> {
            ReadableImage readableImage = new ReadableImage();
            readableImage.setId(img.getId());
            readableImage.setImageName(img.getProductImage());
            readableImage.setDefaultImage(img.isDefaultImage());
            readableImage.setOrder(img.getSortOrder() != null ? img.getSortOrder() : 0);
            
            // Trả về đường dẫn tĩnh cho Frontend
            readableImage.setImageUrl("/uploads/products/" + product.getSku() + "/" + img.getProductImage());
            
            return readableImage;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeProductImage(Long productId, Long imageId) {
        try {
            Product product = productService.retrieveById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + productId));

            ProductImage productImage = productImageService.getById(imageId);
            if (productImage == null || !productImage.getProduct().getId().equals(productId)) {
                throw new ResourceNotFoundException("Không tìm thấy ảnh hoặc ảnh không thuộc sản phẩm này");
            }

            // Gọi Core Service xóa ảnh trong Database và xóa file vật lý
            productImageService.delete(productImage);

        } catch (Exception e) {
            throw new ServiceRuntimeException("Lỗi khi xóa ảnh sản phẩm", e);
        }
    }
}