package or.edu.escuelaing.daniel.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record UserProfileResponse(
        @Schema(description = "Authenticated user identifier", example = "auth0|abc123")
        String id,
        @Schema(description = "Resolved user display name", example = "Daniel R")
        String name,
        @Schema(description = "User email when available", example = "daniel@example.com")
        String email,
        @Schema(description = "Scopes present in current token")
        Set<String> scopes
) {
}
