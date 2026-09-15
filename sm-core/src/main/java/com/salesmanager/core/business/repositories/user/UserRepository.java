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

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups g " +
           "LEFT JOIN FETCH g.permissions " +
           "WHERE u.adminName = :userName")
    Optional<User> findByUserName(@Param("userName") String userName);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups " +
           "WHERE u.id = :id")
    Optional<User> findOneById(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups " +
           "ORDER BY u.id ASC")
    List<User> findAll();

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.groups " +
           "WHERE u.credentialsResetRequest.credentialsRequest = :token")
    Optional<User> findByResetPasswordToken(@Param("token") String token);

    @Query(value = "SELECT u FROM User u WHERE (:email IS NULL OR u.adminEmail LIKE %:email%)",
           countQuery = "SELECT count(u) FROM User u WHERE (:email IS NULL OR u.adminEmail LIKE %:email%)")
    Page<User> listAll(@Param("email") String email, Pageable pageable);
}