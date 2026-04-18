package or.edu.escuelaing.daniel.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record PostResponse(
        @Schema(description = "Unique post identifier", example = "1f860f7e-f77f-4dd4-a7d3-532adf8f4871")
        String id,
        @Schema(description = "Author unique identifier from Auth0", example = "auth0|abc123")
        String authorId,
        @Schema(description = "Display name of the post author", example = "daniel")
        String authorName,
        @Schema(description = "Post body", example = "Hello secure world")
        String content,
        @Schema(description = "Creation timestamp in ISO-8601 format", example = "2026-04-17T18:00:00Z")
        Instant createdAt
) {
}
