package com.pipemasters.server.repository;

import com.pipemasters.server.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends GeneralRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.branch WHERE u.id = :id")
    Optional<User> findByIdWithBranch(@Param("id") Long id);
}
