package com.example.demoRedditFinal.services;

import com.example.demoRedditFinal.DataTransferObject.VoteDto;
import com.example.demoRedditFinal.Exceptions.PostNotFoundException;
import com.example.demoRedditFinal.Exceptions.SpringRedditException;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.Vote;
import com.example.demoRedditFinal.repositories.PostRepository;
import com.example.demoRedditFinal.repositories.VoteRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.*;
import java.util.Optional;

import static com.example.demoRedditFinal.entities.enums.VoteType.UPVOTE;

@Service
@AllArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AuthService authService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void vote(VoteDto voteDto) {
        Post post = postRepository.findById(voteDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Post Not Found with ID - " + voteDto.getPostId()));
        Optional<Vote> voteByPostAndUser = voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUser());
        if (voteByPostAndUser.isPresent() &&
                voteByPostAndUser.get().getVoteType()
                        .equals(voteDto.getVoteType())) {
            throw new SpringRedditException("You have already "
                    + voteDto.getVoteType() + "'d for this post");
        }
        if (UPVOTE.equals(voteDto.getVoteType())) {
            post.setVoteCount(post.getVoteCount() + 1);
        } else {
            post.setVoteCount(post.getVoteCount() - 1);
        }
        insertVote(mapToVote(voteDto, post));
        //voteRepository.save(mapToVote(voteDto, post));
        updateVoteCount(post);
        //postRepository.save(post);
    }

    private Vote mapToVote(VoteDto voteDto, Post post) {
        return Vote.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .user(authService.getCurrentUser())
                .build();
    }

    public Vote insertVote(Vote vote){
        String sql = "INSERT INTO votes " + "(vote_type, post_id, user_id) values (?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, vote.getVoteType().getDirection());
                ps.setLong(2, vote.getPost().getPostId());
                ps.setLong(3, vote.getUser().getUserId());
                return ps;
            }
        }, holder);

        return vote;
    }

    public Post updateVoteCount(Post post){
        String sql = "UPDATE posts\n" +
                "SET vote_count = ?\n" +
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
                ps.setInt(1, post.getVoteCount());
                ps.setLong(2, post.getPostId());
                return ps;
            }
        }, holder);

        return post;
    }
}