package or.edu.escuelaing.daniel.twitter.repository;

import or.edu.escuelaing.daniel.twitter.model.Post;

import java.util.List;

public interface PostRepository {

    Post save(Post post);

    List<Post> findAll();
}
