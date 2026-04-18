package or.edu.escuelaing.daniel.twitter.service;

import or.edu.escuelaing.daniel.twitter.model.AppUser;
import or.edu.escuelaing.daniel.twitter.model.Post;
import or.edu.escuelaing.daniel.twitter.repository.InMemoryPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostServiceTest {

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(new InMemoryPostRepository());
    }

    @Test
    void createPostShouldStoreNewPostAtTopOfStream() {
        AppUser user = new AppUser("auth0|1", "daniel", "daniel@example.com");

        postService.createPost(user, "first");
        postService.createPost(user, "second");

        List<Post> posts = postService.getAllPosts();
        assertEquals(2, posts.size());
        assertEquals("second", posts.getFirst().content());
    }

    @Test
    void createPostShouldRejectBlankContent() {
        AppUser user = new AppUser("auth0|1", "daniel", "daniel@example.com");

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(user, "   "));
    }

    @Test
    void createPostShouldRejectContentLongerThan140Chars() {
        AppUser user = new AppUser("auth0|1", "daniel", "daniel@example.com");
        String tooLong = "x".repeat(141);

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(user, tooLong));
    }
}
