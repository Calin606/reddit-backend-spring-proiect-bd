package com.example.demoRedditFinal.repositories;

import com.example.demoRedditFinal.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    @Query(value = "select * from refresh_tokens r order by created_date DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    RefreshToken findLastTokenByCreatedDate();
}
