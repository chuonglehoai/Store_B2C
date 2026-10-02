package com.salesmanager.core.business.services.catalog.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.category.Category;

public interface CategoryService extends SalesManagerEntityService<Long, Category> {

    void saveOrUpdate(Category category) throws ServiceException;

    void addChild(Category parent, Category child) throws ServiceException;
    
    Optional<Category> getByCode(String code) throws ServiceException;
    
    List<Category> getListByDepth(int depth);
    
    List<Category> getListByLineage(String lineage) throws ServiceException;
    
    Page<Category> getListByName(String name, int page, int count);
}