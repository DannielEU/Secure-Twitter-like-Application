package or.edu.escuelaing.daniel.twitter.repository;

import or.edu.escuelaing.daniel.twitter.model.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private final ConcurrentLinkedDeque<Post> posts = new ConcurrentLinkedDeque<>();

    @Override
    public Post save(Post post) {
        posts.addFirst(post);
        return post;
    }

    @Override
    public List<Post> findAll() {
        return new ArrayList<>(posts);
    }
}
