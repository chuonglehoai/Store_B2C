package com.salesmanager.core.model.common;


public class Criteria {

    // Phân trang chuẩn hiện đại
    private int startPage = 0;
    private int pageSize = 10; 
    
    // Tìm kiếm và Lọc
    private String code;
    private String name;
    private String language;
    private String user;
    private String search;

    // Sắp xếp
    private CriteriaOrderBy orderBy = CriteriaOrderBy.DESC;
    private String criteriaOrderByField;

    // --- Getter và Setter cho Phân trang ---
    public int getStartPage() {
        return startPage;
    }
    

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // --- Getter và Setter cho Lọc dữ liệu ---
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    // --- Getter và Setter cho Sắp xếp ---
    public CriteriaOrderBy getOrderBy() { return orderBy; }
    public void setOrderBy(CriteriaOrderBy orderBy) { this.orderBy = orderBy; }

    public String getCriteriaOrderByField() { return criteriaOrderByField; }
    public void setCriteriaOrderByField(String criteriaOrderByField) { this.criteriaOrderByField = criteriaOrderByField; }
}