package com.example.demoRedditFinal.mapper;

import com.example.demoRedditFinal.DataTransferObject.PostRequest;
import com.example.demoRedditFinal.DataTransferObject.PostResponse;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.Subreddit;
import com.example.demoRedditFinal.entities.User;
import com.example.demoRedditFinal.entities.Vote;
import com.example.demoRedditFinal.entities.enums.VoteType;
import com.example.demoRedditFinal.repositories.CommentRepository;
import com.example.demoRedditFinal.repositories.VoteRepository;
import com.example.demoRedditFinal.services.AuthService;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.example.demoRedditFinal.entities.enums.VoteType.UPVOTE;
import static com.example.demoRedditFinal.entities.enums.VoteType.DOWNVOTE;

@Mapper(componentModel = "spring")
public abstract class PostMapper {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private AuthService authService;

    @Mapping(target = "createdDate",expression = "java(java.time.Instant.now())")
    @Mapping(target = "description",source = "postRequest.description")
    @Mapping(target = "subreddit",source = "subreddit")
    @Mapping(target = "voteCount",constant = "0")
    @Mapping(target = "user",source = "user")
    public abstract Post mapDtoToPost(PostRequest postRequest, Subreddit subreddit, User user);

    @Mapping(target="id",source = "postId")
    @Mapping(target = "subredditName",source = "subreddit.name")
    @Mapping(target="userName",source = "user.username")
    @Mapping(target="commentCount",expression = "java(commentCount(post))")
    @Mapping(target = "duration",expression = "java(getDuration(post))")
    @Mapping(target = "upVote",expression = "java(isPostUpVoted(post))")
    @Mapping(target = "downVote",expression = "java(isPostDownVoted(post))")
    public abstract PostResponse mapPostToDto(Post post);

    Integer commentCount(Post post){
        return commentRepository.findByPost(post).size();
    }

    String getDuration(Post post){
        return TimeAgo.using(post.getCreatedDate().toEpochMilli());
    }

    boolean isPostUpVoted(Post post){
        return checkVoteType(post,UPVOTE);
    }

    boolean isPostDownVoted(Post post){
        return checkVoteType(post,DOWNVOTE);
    }

    private boolean checkVoteType(Post post, VoteType voteType){
        if(authService.isLoggedIn()){
            Optional<Vote> voteForPostByUser=voteRepository.findTopByPostAndUserOrderByVoteIdDesc(
                    post,
                    authService.getCurrentUser()
            );

            return voteForPostByUser.filter(vote -> vote.getVoteType().equals(voteType)).isPresent();
        }
        return false;
    }


}
