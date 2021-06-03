package com.example.demoRedditFinal.DataTransferObject;

import com.example.demoRedditFinal.entities.enums.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteDto {
    private VoteType voteType;
    private Long postId;
}
