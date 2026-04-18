package or.edu.escuelaing.daniel.twitter.service;

import or.edu.escuelaing.daniel.twitter.model.AppUser;
import or.edu.escuelaing.daniel.twitter.model.Post;
import or.edu.escuelaing.daniel.twitter.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    public static final int MAX_POST_LENGTH = 140;

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(AppUser user, String content) {
        String normalizedContent = content == null ? "" : content.trim();

        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException("Post content must not be blank");
        }

        if (normalizedContent.length() > MAX_POST_LENGTH) {
            throw new IllegalArgumentException("Post content must be at most 140 characters");
        }

        Post post = new Post(
                UUID.randomUUID().toString(),
                user.id(),
                user.displayName(),
                normalizedContent,
                Instant.now()
        );

        return postRepository.save(post);
    }
}
