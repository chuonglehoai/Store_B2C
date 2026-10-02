package com.salesmanager.core.business.services.catalog.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.configuration.events.category.CategoryDeletedEvent;
import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.catalog.category.CategoryRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.Product;

@Service("categoryService")
public class CategoryServiceImpl extends SalesManagerEntityServiceImpl<Long, Category> implements CategoryService {

    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Inject
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void create(Category category) throws ServiceException {
        super.create(category); 
        
        StringBuilder lineage = new StringBuilder();
        Category parent = category.getParent();
        
        if (parent != null && parent.getId() != null && parent.getId() != 0) {
            Category p = this.getById(parent.getId());
            lineage.append(p.getLineage()).append(category.getId()).append("/");
            category.setDepth(p.getDepth() + 1);
        } else {
            lineage.append("/").append(category.getId()).append("/");
            category.setDepth(0);
        }
        category.setLineage(lineage.toString());
        
        super.update(category); 
    }

    @Override
    public void saveOrUpdate(Category category) throws ServiceException {
        if (category.getId() != null && category.getId() > 0) {
            super.update(category);
        } else {
            this.create(category);
        }
    }

    @Override
    public void addChild(Category parent, Category child) throws ServiceException {
        if (child == null) {
            throw new ServiceException("Child category should not be null");
        }

        try {
            if (parent == null) {
                child.setParent(null);
                child.setDepth(0);
                child.setLineage(new StringBuilder().append("/").append(child.getId()).append("/").toString());
            } else {
                Category p = this.getById(parent.getId());
                String lineage = p.getLineage();
                int depth = p.getDepth();

                child.setParent(p);
                child.setDepth(depth + 1);
                child.setLineage(new StringBuilder().append(lineage).append(child.getId()).append("/").toString());
            }

            update(child);

            StringBuilder childLineage = new StringBuilder();
            childLineage.append(child.getLineage()).append(child.getId()).append("/");
            List<Category> subCategories = getListByLineage(childLineage.toString());

            if (subCategories != null && subCategories.size() > 0) {
                for (Category subCategory : subCategories) {
                    if (!child.getId().equals(subCategory.getId())) {
                        addChild(child, subCategory);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void delete(Category category) throws ServiceException {
        // Lấy tất cả danh mục conđể xóa đồng bộ
        StringBuilder lineage = new StringBuilder();
        lineage.append(category.getLineage()).append(category.getId()).append(Constants.SLASH);
        List<Category> categories = this.getListByLineage(lineage.toString());

        Category dbCategory = getById(category.getId());

        if (dbCategory != null) {
            // Bước 1: Xóa danh mục trong Database (chuyên môn của CategoryService)
            categoryRepository.delete(dbCategory);

            // Bước 2: Phát loa thông báo "Đã xóa danh mục này rồi nhé!"
            eventPublisher.publishEvent(new CategoryDeletedEvent(this, dbCategory));
        }
    }

    @Override
    public Optional<Category> getByCode(String code) throws ServiceException {
        return categoryRepository.findByCode(code);
    }

    @Override
    public List<Category> getListByLineage(String lineage) throws ServiceException {
        return categoryRepository.findByLineage(lineage);
    }

    @Override
    public List<Category> getListByDepth(int depth) {
        return categoryRepository.findByDepth(depth);
    }

    @Override
    public Page<Category> getListByName(String name, int page, int count) {
        Pageable pageRequest = PageRequest.of(page, count);
        return categoryRepository.findByName(name, pageRequest);
    }
}