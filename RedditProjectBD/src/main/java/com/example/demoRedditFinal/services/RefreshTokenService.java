package com.example.demoRedditFinal.services;

import com.example.demoRedditFinal.Exceptions.SpringRedditException;
import com.example.demoRedditFinal.entities.RefreshToken;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.entities.VerificationToken;
import com.example.demoRedditFinal.repositories.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JdbcTemplate jdbcTemplate;

    public RefreshToken generateRefreshToken(){
        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedDate(Instant.now());

        //return refreshTokenRepository.save(refreshToken);
        return insertRefreshToken(refreshToken);
    }

    void validateRefreshToken(String token){
        refreshTokenRepository.findByToken(token)
                .orElseThrow(()->new SpringRedditException("Invalid Refresh Token"));
    }

    public void deleteRefreshToken(String token){
        //refreshTokenRepository.deleteByToken(token);

        deleteToken(refreshTokenRepository.findLastTokenByCreatedDate().getToken());
    }

    public void deleteToken(String refreshToken)
    {

        String sql = "delete from refresh_tokens where token=?";
        KeyHolder holder = new GeneratedKeyHolder();
        System.out.println(refreshToken+" nu e de gasit "+jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, refreshToken);
            return ps;
        }, holder));

    }

    public RefreshToken insertRefreshToken(RefreshToken refreshToken)
    {
        String sql = "INSERT INTO refresh_tokens " + "(created_date, token) values (?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new java.sql.Date(Date.from(refreshToken.getCreatedDate()).getTime()));
                ps.setString(2, refreshToken.getToken());
                return ps;
            }
        }, holder);

        return refreshToken;
    }

}
