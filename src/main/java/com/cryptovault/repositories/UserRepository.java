package com.cryptovault.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cryptovault.models.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.email = :email")
    User getByEmail(String email);
    @Query("select u from User u where u.id = :id")
    User getById(int id);
    @Query("select u from User u where u.username = :username")
    List<User> getByUsername(String username);
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO users(username, email, passwordHash) VALUES (:#{#user.username}, :#{#user.email}, :#{#user.passwordHash})", nativeQuery = true)
    User create(User user);
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.username = :#{#user.username}, u.email = :#{#user.email}, u.passwordHash = :#{#user.passwordHash} WHERE u.id = :#{#user.id}")
    User update (User user);
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    void delete (int id);
}
