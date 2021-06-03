package com.example.demoRedditFinal.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    private Long postId;
    private String subredditName;
    private String postName;
    private String description;
    private String url;

}
