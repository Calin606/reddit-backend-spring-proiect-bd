package com.example.demoRedditFinal.repositories;

import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.Subreddit;
import com.example.demoRedditFinal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select * from posts", nativeQuery = true)
    List<Post> findAllPosts();

    @Query(value = "select * from posts p where p.id like %:subreddit%", nativeQuery = true)
    List<Post> findAllBySubreddit(Subreddit subreddit);

    @Query(value = "select * from posts p where p.user_id like %:user%", nativeQuery = true)
    List<Post> findByUser(User user);

}
