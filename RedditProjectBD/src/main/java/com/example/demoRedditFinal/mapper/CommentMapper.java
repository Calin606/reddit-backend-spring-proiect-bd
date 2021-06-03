package com.example.demoRedditFinal.mapper;

import com.example.demoRedditFinal.DataTransferObject.CommentsDto;
import com.example.demoRedditFinal.entities.Comment;
import com.example.demoRedditFinal.entities.Post;
import com.example.demoRedditFinal.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "commentsDto.text")
    @Mapping(target = "createdDate",expression = "java(java.time.Instant.now())")
    @Mapping(target = "post",source = "post")
    @Mapping(target = "user",source = "user")
    Comment mapDtoToComment(CommentsDto commentsDto, Post post, User user);

    @Mapping(target = "postId",expression = "java(comment.getPost().getPostId())")
    @Mapping(target = "userName",expression = "java(comment.getUser().getUsername())")
    CommentsDto mapCommentToDto(Comment comment);

}
