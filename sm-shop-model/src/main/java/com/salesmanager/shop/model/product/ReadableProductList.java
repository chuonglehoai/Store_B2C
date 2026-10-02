package com.salesmanager.shop.model.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO dùng để trả về danh sách Sản phẩm kèm thông tin phân trang (Pagination)
 */
public class ReadableProductList implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<ReadableProduct> products = new ArrayList<>();
    private int totalPages;
    private long recordsTotal;
    private int number; // Số lượng phần tử trong trang hiện tại

    // Getters and Setters
    public List<ReadableProduct> getProducts() { return products; }
    public void setProducts(List<ReadableProduct> products) { this.products = products; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getRecordsTotal() { return recordsTotal; }
    public void setRecordsTotal(long recordsTotal) { this.recordsTotal = recordsTotal; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}