package com.salesmanager.core.business.repositories.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.model.customer.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.groups WHERE c.nick = :nick")
    Optional<Customer> findByNick(@Param("nick") String nick);

    Optional<Customer> findByUserName(String userName);

    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.groups WHERE c.emailAddress = :email")
    Optional<Customer> findByEmailAddress(@Param("email") String email);

    // Bổ sung: Tìm kiếm danh sách khách hàng theo tên (có fetch groups)
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.groups " +
           "WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<Customer> findByName(@Param("fullName") String name);

    // Bổ sung: Tìm kiếm theo mã token khôi phục mật khẩu
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.groups " +
           "WHERE c.credentialsResetRequest.credentialsRequest = :token")
    Optional<Customer> findByResetPasswordToken(@Param("token") String token);

    @Query(value = "SELECT c FROM Customer c WHERE (:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.emailAddress) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT count(c) FROM Customer c WHERE (:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.emailAddress) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> listAll(@Param("search") String search, Pageable pageable);
}