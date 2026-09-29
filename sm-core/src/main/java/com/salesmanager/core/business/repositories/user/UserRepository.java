package com.salesmanager.core.business.repositories.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.groups g LEFT JOIN FETCH g.permissions WHERE u.adminUserName = ?1")
       Optional<User> findByUserName(String userName);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups " +
           "WHERE u.id = :id")
    Optional<User> findOneById(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups " +
           "ORDER BY u.id ASC")
    List<User> findAll();

    // Xóa thẻ @Query("...") ở trên dòng này đi
    User findByResetPasswordToken(String resetPasswordToken);

    @Query(value = "SELECT u FROM User u WHERE " +
                   "(:email IS NULL OR u.adminEmail LIKE %:email% OR u.adminUserName LIKE %:email%) AND " +
                   "(:name IS NULL OR u.adminName LIKE %:name%)",
           countQuery = "SELECT count(u) FROM User u WHERE " +
                        "(:email IS NULL OR u.adminEmail LIKE %:email% OR u.adminUserName LIKE %:email%) AND " +
                        "(:name IS NULL OR u.adminName LIKE %:name%)")
    Page<User> listAll(@Param("email") String email, @Param("name") String name, Pageable pageable);
}