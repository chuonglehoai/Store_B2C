package com.salesmanager.core.business.configuration.events.category;

import org.springframework.context.ApplicationEvent;
import com.salesmanager.core.model.catalog.category.Category;

public class CategoryDeletedEvent extends ApplicationEvent {
    
    private final Category category;

    public CategoryDeletedEvent(Object source, Category category) {
        super(source);
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}