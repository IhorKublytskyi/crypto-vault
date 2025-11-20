package com.cryptovault.abstractions;

import com.cryptovault.models.Key;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IKeyRepository extends JpaRepository<Key, Long> {
}
