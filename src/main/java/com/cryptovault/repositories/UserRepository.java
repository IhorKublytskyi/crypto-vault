package com.cryptovault.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cryptovault.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    //no Queries needed for basic CRUD operations
    Optional<User> findByEmail(String email);

    List<User> findByUsername(String username);

    boolean existsByEmail(String email);


    // findById(Long id) and save(User user) and other basic operations are already provided by JpaRepository
}


