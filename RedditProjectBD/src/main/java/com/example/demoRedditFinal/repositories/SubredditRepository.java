package com.example.demoRedditFinal.repositories;

import com.example.demoRedditFinal.DataTransferObject.SubredditDto;
import com.example.demoRedditFinal.entities.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long> {

    @Query(value = "select * from subreddits", nativeQuery = true)
    List<Subreddit> findAllSubreddits();

    @Query(value = "select * from subreddits s where s.id like %:sid%", nativeQuery = true)
    Optional<Subreddit> getSubredditById(Long sid);

    Optional<Subreddit> findByName(String subredditName);

    @Query(value = "select * from subreddits s order by s.name", nativeQuery = true)
    List<Subreddit> orderSubredditsAsc();

    @Query(value = "select * from subreddits s order by s.name desc", nativeQuery = true)
    List<Subreddit> orderSubredditsDesc();
}
