package com.example.fooddeliveryaut.repository;

import com.example.fooddeliveryaut.dto.UserProjection;
import com.example.fooddeliveryaut.enums.UserRole;
import com.example.fooddeliveryaut.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Обычные методы
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 🎯 Projection методы с @Query
    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u WHERE u.email = :email")
    Optional<UserProjection> findProjectionByEmail(@Param("email") String email);

    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u")
    List<UserProjection> findAllProjections();

    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u WHERE u.id = :id")
    Optional<UserProjection> findProjectionById(@Param("id") Long id);

    // Дополнительные проекции по роли
    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u WHERE u.userRole = :role")
    List<UserProjection> findProjectionsByRole(@Param("role") UserRole role);
}