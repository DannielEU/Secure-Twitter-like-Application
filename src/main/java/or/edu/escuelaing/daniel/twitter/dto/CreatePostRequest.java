package or.edu.escuelaing.daniel.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a post")
public class CreatePostRequest {

    @Schema(
            description = "Post content with maximum 140 characters",
            example = "Building secure APIs with Auth0 is awesome!",
            maxLength = 140
    )
    @NotBlank(message = "content is required")
    @Size(max = 140, message = "content must not exceed 140 characters")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
