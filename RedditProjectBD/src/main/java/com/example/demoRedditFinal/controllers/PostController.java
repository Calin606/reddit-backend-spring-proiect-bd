package com.example.demoRedditFinal.controllers;

import com.example.demoRedditFinal.DataTransferObject.PostRequest;
import com.example.demoRedditFinal.DataTransferObject.PostResponse;
import com.example.demoRedditFinal.services.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/posts/")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody PostRequest postRequest) {
        postService.save(postRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return status(HttpStatus.OK).body(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return status(HttpStatus.OK).body(postService.getPost(id));
    }

    @GetMapping("by-subreddit/{id}")
    public ResponseEntity<List<PostResponse>> getPostsBySubreddit(@PathVariable Long id) {
        return status(HttpStatus.OK).body(postService.getPostsBySubreddit(id));
    }

    @GetMapping("by-user/{name}")
    public ResponseEntity<List<PostResponse>> getPostsByUsername(@PathVariable String name) {
        return status(HttpStatus.OK).body(postService.getPostsByUsername(name));
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updatePost(@RequestBody PostRequest postRequest) {
        postService.update(postRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/remove/{pid}")
    public ResponseEntity<String> deletePostById(@PathVariable Long pid){
        return status(HttpStatus.OK).body(postService.deletePostById(pid));
    }
}
