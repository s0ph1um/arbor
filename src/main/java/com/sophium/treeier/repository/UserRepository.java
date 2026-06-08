package com.sophium.treeier.repository;

import com.sophium.treeier.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
        "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByEmailOrName(@Param("query") String query);

}
