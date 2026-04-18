package or.edu.escuelaing.daniel.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record StreamResponse(
        @Schema(description = "Total posts currently in the stream", example = "2")
        int totalPosts,
        @Schema(description = "Posts in reverse chronological order")
        List<PostResponse> posts
) {
}
