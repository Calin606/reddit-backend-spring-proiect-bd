package com.example.demoRedditFinal.repositories;

import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findTopByPostAndUserOrderByVoteIdDesc(Post post, User currentUser);
}
