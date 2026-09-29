package com.salesmanager.shop.model.security;

public class PersistableGroup extends GroupEntity {

  private static final long serialVersionUID = 1L;
  
  private Long id;
  
  public PersistableGroup() {}
  
  public PersistableGroup(String name) {
    super.setName(name);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}