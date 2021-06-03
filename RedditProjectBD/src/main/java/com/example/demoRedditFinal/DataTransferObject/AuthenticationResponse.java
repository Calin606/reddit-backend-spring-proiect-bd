package com.example.demoRedditFinal.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class AuthenticationResponse {



    private String authenticationToken;
    private String refreshToken;
    private Instant expiresAt;
    private String username;
}
