package com.learning.database.repository;

import com.learning.database.entity.relationship.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    // JOIN FETCH loads User + Address in one query (prevents N+1 on the OneToOne)
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.address")
    java.util.List<UserEntity> findAllWithAddress();
}
