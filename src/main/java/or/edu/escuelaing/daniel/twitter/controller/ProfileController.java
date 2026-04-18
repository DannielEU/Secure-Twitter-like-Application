package or.edu.escuelaing.daniel.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import or.edu.escuelaing.daniel.twitter.dto.UserProfileResponse;
import or.edu.escuelaing.daniel.twitter.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Profile", description = "Authenticated user profile endpoint")
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns user information derived from the current JWT token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public UserProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userProfileService.fromJwt(jwt);
    }
}
