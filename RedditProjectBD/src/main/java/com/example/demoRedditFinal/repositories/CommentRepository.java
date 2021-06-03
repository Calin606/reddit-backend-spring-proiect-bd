package com.example.demoRedditFinal.repositories;

import com.example.demoRedditFinal.entities.Comment;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);
}
