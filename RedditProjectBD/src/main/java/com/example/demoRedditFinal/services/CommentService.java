package com.example.demoRedditFinal.services;

import com.example.demoRedditFinal.DataTransferObject.CommentsDto;
import com.example.demoRedditFinal.Exceptions.PostNotFoundException;
import com.example.demoRedditFinal.entities.Comment;
import com.example.demoRedditFinal.entities.NotificationEmail;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.mapper.CommentMapper;
import com.example.demoRedditFinal.repositories.CommentRepository;
import com.example.demoRedditFinal.repositories.PostRepository;
import com.example.demoRedditFinal.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CommentService {
    private static final String POST_URL = "";
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final MailContentBuilderService mailContentBuilder;
    private final MailService mailService;

    private final JdbcTemplate jdbcTemplate;

    public void save(CommentsDto commentsDto) {
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
        Comment comment = commentMapper.mapDtoToComment(commentsDto, post, authService.getCurrentUser());
        commentRepository.save(comment);

        String message = mailContentBuilder.build(authService.getCurrentUser() + " posted a comment on your post." + POST_URL);
        sendCommentNotification(message, post.getUser());
    }

    private void sendCommentNotification(String message, User user) {
        mailService.sendMail(new NotificationEmail(user.getUsername() + " Commented on your post", user.getEmail(), message));
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId.toString()));
        return commentRepository.findByPost(post)
                .stream()
                .map(commentMapper::mapCommentToDto).collect(toList());
    }

    public List<CommentsDto> getAllCommentsForUser(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        return commentRepository.findAllByUser(user)
                .stream()
                .map(commentMapper::mapCommentToDto)
                .collect(toList());
    }

    public void update(CommentsDto commentsDto) {
        updateComment(commentsDto);
    }

    public CommentsDto updateComment(CommentsDto commentsDto){
        String sql = "UPDATE comments\n" +
                "SET text = ? \n" +
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
                ps.setString(1, commentsDto.getText());
                ps.setLong(2, commentsDto.getPostId());
                return ps;
            }
        }, holder);

        return commentsDto;
    }
}
