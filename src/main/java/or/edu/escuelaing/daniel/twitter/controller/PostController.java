package or.edu.escuelaing.daniel.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import or.edu.escuelaing.daniel.twitter.dto.CreatePostRequest;
import or.edu.escuelaing.daniel.twitter.dto.PostResponse;
import or.edu.escuelaing.daniel.twitter.dto.StreamResponse;
import or.edu.escuelaing.daniel.twitter.model.AppUser;
import or.edu.escuelaing.daniel.twitter.model.Post;
import or.edu.escuelaing.daniel.twitter.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Posts", description = "Public stream and post creation endpoints")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    @Operation(
            summary = "Get public posts",
            description = "Returns all posts from the single public stream",
            security = {}
    )
    public ResponseEntity<List<PostResponse>> getPosts() {
        List<PostResponse> posts = postService.getAllPosts().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/stream")
    @Operation(
            summary = "Get global stream",
            description = "Returns stream metadata and all posts in reverse chronological order",
            security = {}
    )
    public ResponseEntity<StreamResponse> getStream() {
        List<PostResponse> posts = postService.getAllPosts().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new StreamResponse(posts.size(), posts));
    }

    @PostMapping("/posts")
    @Operation(
            summary = "Create a post",
            description = "Creates a new post with max 140 characters. Requires write:posts scope.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AppUser user = AppUser.fromJwt(jwt);
        Post created = postService.createPost(user, request.getContent());

        return ResponseEntity
                .created(URI.create("/api/posts/" + created.id()))
                .body(toResponse(created));
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.id(),
                post.authorId(),
                post.authorName(),
                post.content(),
                post.createdAt()
        );
    }
}
