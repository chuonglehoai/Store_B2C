package com.salesmanager.core.business.repositories.user;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.user.Permission;
import com.salesmanager.core.model.user.PermissionCriteria;
import com.salesmanager.core.model.user.PermissionList;

@Repository
public class PermissionRepositoryImpl implements PermissionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public PermissionList listByCriteria(PermissionCriteria criteria) {
        PermissionList permissionList = new PermissionList();

        // ==========================================
        // 1. TẠO TRUY VẤN ĐẾM TỔNG SỐ LƯỢNG (COUNT)
        // ==========================================
        StringBuilder countBuilderSelect = new StringBuilder();
        countBuilderSelect.append("select count(p) from Permission as p");
        
        StringBuilder countBuilderWhere = new StringBuilder();
        
        if (criteria.getGroupIds() != null && criteria.getGroupIds().size() > 0) {
            countBuilderSelect.append(" INNER JOIN p.groups grous");
            countBuilderWhere.append(" where grous.id in (:cid)");
        }
        
        Query countQ = em.createQuery(
                countBuilderSelect.toString() + countBuilderWhere.toString());

        if (criteria.getGroupIds() != null && criteria.getGroupIds().size() > 0) {
            countQ.setParameter("cid", criteria.getGroupIds());
        }
        
        Number count = (Number) countQ.getSingleResult();
        permissionList.setTotalCount(count.intValue());
        
        if (count.intValue() == 0) {
            return permissionList;
        }

        // ==========================================
        // 2. TẠO TRUY VẤN LẤY DỮ LIỆU (FETCH)
        // ==========================================
        StringBuilder qs = new StringBuilder();
        qs.append("select p from Permission as p ");
        qs.append("join fetch p.groups grous ");
        
        if (criteria.getGroupIds() != null && criteria.getGroupIds().size() > 0) {
            qs.append(" where grous.id in (:cid)");
        }
        
        qs.append(" order by p.id asc ");
        
        String hql = qs.toString();
        Query q = em.createQuery(hql);

        if (criteria.getGroupIds() != null && criteria.getGroupIds().size() > 0) {
            q.setParameter("cid", criteria.getGroupIds());
        }
        
        // ==========================================
        // 3. CHỨC NĂNG PHÂN TRANG (Cập nhật theo Criteria mới)
        // ==========================================
        // Đổi getMaxCount() thành getPageSize()
        if (criteria.getPageSize() > 0) {
            
            // TÍNH TOÁN OFFSET: Vị trí bắt đầu = Trang hiện tại * Số lượng/Trang
            // Ví dụ: Trang 0 * 10 = bắt đầu từ 0. Trang 1 * 10 = bắt đầu từ 10.
            int offset = criteria.getStartPage() * criteria.getPageSize();
            q.setFirstResult(offset);
            
            // Giữ nguyên logic cũ của bạn để không làm mất chức năng
            if (criteria.getPageSize() < count.intValue()) {
                q.setMaxResults(criteria.getPageSize());
                permissionList.setTotalCount(criteria.getPageSize()); 
            } else {
                q.setMaxResults(count.intValue());
                permissionList.setTotalCount(count.intValue());
            }
        }
        
        @SuppressWarnings("unchecked")
        List<Permission> permissions = q.getResultList();
        permissionList.setPermissions(permissions);
        
        return permissionList;
    }   
}