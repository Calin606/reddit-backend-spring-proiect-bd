package com.example.demoRedditFinal.services;

import com.example.demoRedditFinal.DataTransferObject.AuthenticationResponse;
import com.example.demoRedditFinal.DataTransferObject.LoginRequest;
import com.example.demoRedditFinal.DataTransferObject.RefreshTokenRequest;
import com.example.demoRedditFinal.DataTransferObject.RegisterRequest;
import com.example.demoRedditFinal.Exceptions.SpringRedditException;
import com.example.demoRedditFinal.entities.NotificationEmail;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.entities.VerificationToken;
import com.example.demoRedditFinal.repositories.UserRepository;
import com.example.demoRedditFinal.repositories.VerificationTokenRepository;
import com.example.demoRedditFinal.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;



@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    private final JdbcTemplate jdbcTemplate;

    public void signup(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);
        //user = insertUser(user);
        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8082/api/auth/accountVerification/" + token));
    }

    public void deleteAccount(String username) {

        Optional<User> user = userRepository.findByUsername(username);
        deleteVerificationTokensByUserId(user.get().getUserId());
        deleteVotesByUserId(user.get().getUserId());
        deleteUserByUsername(username);

    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        insertVerificationToken(verificationToken);
        //verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
                //findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token")));
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }


    public User insertUser(User user)
    {
        String sql = "INSERT INTO users " + "(created, email, enabled, password, username) VALUES (?, ?, ?,?,?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new Date(6));
                ps.setString(2, user.getEmail());
                ps.setBoolean(3, user.isEnabled());
                ps.setString(4,user.getPassword());
                ps.setString(5, user.getUsername());
                return ps;
            }
        }, holder);


        return user;
    }

    public VerificationToken insertVerificationToken(VerificationToken verificationToken)
    {
        String sql = "INSERT INTO verification_tokens " + "(expiring_date, token, user_user_id) values (?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setDate(1, new java.sql.Date(new java.util.Date().getTime() + (1000 * 60 * 60 * 24)));
                ps.setString(2, verificationToken.getToken());
                ps.setLong(3, verificationToken.getUser().getUserId());
                return ps;
            }
        }, holder);

        return verificationToken;
    }

    public Optional<VerificationToken>  findByToken(String token){
        String sql = "select * from verification_tokens where token = ?";
        KeyHolder holder = new GeneratedKeyHolder();
        int res = jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, token);
                return ps;
            }
        }, holder);
        System.out.println(holder);
        verificationTokenRepository.findById(1L);
        return (res == 0)? Optional.empty() : Optional.of(new VerificationToken());
    }

    public void deleteUserByUsername(String username)
    {
        String sql = "DELETE FROM users WHERE username=?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, username);

                return ps;
            }
        }, holder);

    }

    public void deletePostByUserId(Long id)
    {
        String sql = "DELETE FROM posts WHERE user_id=?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, id);

                return ps;
            }
        }, holder);

    }


    public void deleteSubredditByUserId(Long id)
    {
        String sql = "DELETE FROM subreddits WHERE user_user_id=?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, id);

                return ps;
            }
        }, holder);

    }

    public void deleteVotesByUserId(Long id)
    {
        String sql = "DELETE FROM votes WHERE user_id=?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, id);

                return ps;
            }
        }, holder);

        deletePostByUserId(id);
        deleteSubredditByUserId(id);

    }

    public void deleteVerificationTokensByUserId(Long id)
    {
        String sql = "DELETE FROM verification_tokens WHERE user_user_id=?";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator()
        {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException
            {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, id);

                return ps;
            }
        }, holder);


    }


}
