package com.example.demoRedditFinal.services;

import com.example.demoRedditFinal.DataTransferObject.CommentsDto;
import com.example.demoRedditFinal.DataTransferObject.PostRequest;
import com.example.demoRedditFinal.DataTransferObject.PostResponse;
import com.example.demoRedditFinal.Exceptions.PostNotFoundException;
import com.example.demoRedditFinal.Exceptions.SubredditNotFoundException;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.Subreddit;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.entities.VerificationToken;
import com.example.demoRedditFinal.mapper.PostMapper;
import com.example.demoRedditFinal.repositories.PostRepository;
import com.example.demoRedditFinal.repositories.SubredditRepository;
import com.example.demoRedditFinal.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PostMapper postMapper;
    private final JdbcTemplate jdbcTemplate;

    public void save(PostRequest postRequest) {
        System.out.println("Nume: "+postRequest.getSubredditName());
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new SubredditNotFoundException(postRequest.getSubredditName()));
        //postRepository.save(postMapper.mapDtoToPost(postRequest, subreddit, authService.getCurrentUser()));
        insertPost(postMapper.mapDtoToPost(postRequest, subreddit, authService.getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id.toString()));
        return postMapper.mapPostToDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllPosts()
                .stream()
                .map(postMapper::mapPostToDto)
                .collect(toList());
    }

    public void update(PostRequest postRequest) {
        updatePost(postRequest);
    }

    public String deletePostById(Long pid) {

        postRepository.deleteById(pid);
        return "Deleted successfully";

    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return posts.stream().map(postMapper::mapPostToDto).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return postRepository.findByUser(user)
                .stream()
                .map(postMapper::mapPostToDto)
                .collect(toList());
    }

    public Post insertPost(Post post)
    {
        String sql = "INSERT INTO posts " + "(created_date, description, post_name, url, vote_count, id, user_id) values (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new java.sql.Date(Date.from(post.getCreatedDate()).getTime()));
                ps.setString(2, post.getDescription());
                ps.setString(3, post.getPostName());
                ps.setString(4, post.getUrl());
                ps.setInt(5, post.getVoteCount());
                ps.setLong(6, post.getSubreddit().getId());
                ps.setLong(7, post.getUser().getUserId());
                return ps;
            }
        }, holder);

        return post;
    }

    public PostRequest updatePost(PostRequest postRequest){
        String sql = "UPDATE posts\n" +
                "SET description = ? \n" +
                "WHERE post_id = ?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, postRequest.getDescription());
                ps.setLong(2, postRequest.getPostId());
                return ps;
            }
        }, holder);

        return postRequest;
    }


}
