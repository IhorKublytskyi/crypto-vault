package com.cryptovault.abstractions;

import com.cryptovault.models.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IKeyRepository extends JpaRepository<Key, Long> {
    List<Key> findByUserId(Long userId);

    Optional<Key> findByIdAndUserId(Long id, Long userId);

    List<Key> findByUserIdAndType(Long userId, Key.KeyType type);

    boolean existsByUserIdAndType(Long userId, Key.KeyType type);

    Optional<Key> findByUserIdAndTypeAndAlgorithm(Long userId, Key.KeyType type, Key.Algorithm algorithm);

    @Query("SELECT k FROM Key k WHERE k.user.id = :userId AND k.createdAt > :date")
    List<Key> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    @Query(value = "SELECT type, COUNT(*) as count FROM keys WHERE user_id = :userId GROUP BY type", nativeQuery = true)
    List<Object[]> countKeysByTypeForUser(@Param("userId") Long userId);
}
