package com.example.demoRedditFinal.controllers;

import com.example.demoRedditFinal.DataTransferObject.AuthenticationResponse;
import com.example.demoRedditFinal.DataTransferObject.LoginRequest;
import com.example.demoRedditFinal.DataTransferObject.RefreshTokenRequest;
import com.example.demoRedditFinal.DataTransferObject.RegisterRequest;
import com.example.demoRedditFinal.services.AuthService;
import com.example.demoRedditFinal.services.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody RegisterRequest registerRequest) {
        authService.signup(registerRequest);
        return new ResponseEntity(OK);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest) throws UnsupportedEncodingException {
        return authService.login(loginRequest);
    }

    @GetMapping("accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        authService.verifyAccount(token);
        return new ResponseEntity<>("Account Activated Successully", OK);
    }


    @PostMapping("/refresh/token")
    public AuthenticationResponse refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        return authService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(
                "Refresh Token Deleted Successfully!!"
        );
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteAccount(@PathVariable String username){
        System.out.println(
                username
        );
        authService.deleteAccount(username);
        return ResponseEntity.status(OK).body("Account deleted successfully");
    }

}
