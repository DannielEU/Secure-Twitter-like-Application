package or.edu.escuelaing.daniel.twitter.model;

import java.time.Instant;

public record Post(
        String id,
        String authorId,
        String authorName,
        String content,
        Instant createdAt
) {
}
