package or.edu.escuelaing.daniel.twitter.model;

import org.springframework.security.oauth2.jwt.Jwt;

public record AppUser(String id, String displayName, String email) {

    public static AppUser fromJwt(Jwt jwt) {
        String id = jwt.getSubject();
        String displayName = firstNonBlank(
                jwt.getClaimAsString("nickname"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("preferred_username"),
                id
        );
        String email = jwt.getClaimAsString("email");

        return new AppUser(id, displayName, email);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "unknown";
    }
}
