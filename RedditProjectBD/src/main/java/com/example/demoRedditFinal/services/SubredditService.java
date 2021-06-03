package com.example.demoRedditFinal.services;


import com.example.demoRedditFinal.DataTransferObject.SubredditDto;
import com.example.demoRedditFinal.Exceptions.SpringRedditException;
import com.example.demoRedditFinal.Exceptions.SubredditNotFoundException;
import com.example.demoRedditFinal.entities.Subreddit;
import com.example.demoRedditFinal.mapper.SubredditMapper;
import com.example.demoRedditFinal.repositories.SubredditRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.time.Instant.now;

@Service
@AllArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final AuthService authService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<SubredditDto> getAll() {
        return subredditRepository.findAllSubreddits()
                .stream()
                .map(this::mapToDto)
                .collect(toList());
    }

    @Transactional
    public SubredditDto save(SubredditDto subredditDto) {
        Subreddit subreddit = insertSubreddit(mapToSubreddit(subredditDto));
        subredditDto.setId(subreddit.getId());
        return subredditDto;
    }

    @Transactional(readOnly = true)
    public SubredditDto getSubreddit(Long id) {
        Subreddit subreddit = subredditRepository.getSubredditById(id)
                .orElseThrow(() -> new SubredditNotFoundException("Subreddit not found with id -" + id));
        return mapToDto(subreddit);
    }

    private SubredditDto mapToDto(Subreddit subreddit) {
        return SubredditDto.builder().name(subreddit.getName())
                .id(subreddit.getId())
                .postCount(subreddit.getPosts().size())
                .build();
    }

    private Subreddit mapToSubreddit(SubredditDto subredditDto) {
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Subreddit.builder().name("/r/" + subredditDto.getName())
                .description(subredditDto.getDescription())
                .user(authService.getCurrentUser())
                .createdDate(now()).build();
    }

    public Subreddit insertSubreddit(Subreddit subreddit)
    {
        String sql = "INSERT INTO subreddits " + "(created_date, description, name, user_user_id) VALUES (?, ?, ?,?)";
        KeyHolder holder = new GeneratedKeyHolder();
        Number userId = subreddit.getUser().getUserId();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new Date(6));
                ps.setString(2, subreddit.getDescription());
                ps.setString(3, subreddit.getName());
                ps.setString(4,userId.toString());
                return ps;
            }
        }, holder);

        //int generatedEmployeeId = holder.getKey().intValue();
        //System.out.println("generatedEmployeeId = " + generatedEmployeeId);
        return subreddit;
    }


}
