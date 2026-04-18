package or.edu.escuelaing.daniel.twitter.service;

import or.edu.escuelaing.daniel.twitter.dto.UserProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UserProfileService {

    public UserProfileResponse fromJwt(Jwt jwt) {
        String name = firstNonBlank(
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("nickname"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getSubject()
        );

        Set<String> scopes = extractScopes(jwt);

        return new UserProfileResponse(
                jwt.getSubject(),
                name,
                jwt.getClaimAsString("email"),
                scopes
        );
    }

    private Set<String> extractScopes(Jwt jwt) {
        LinkedHashSet<String> scopeSet = new LinkedHashSet<>();

        String scopeClaim = jwt.getClaimAsString("scope");
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            scopeSet.addAll(Arrays.asList(scopeClaim.split("\\s+")));
        }

        Object scpClaim = jwt.getClaims().get("scp");
        if (scpClaim instanceof Collection<?> collection) {
            for (Object entry : collection) {
                if (entry != null) {
                    scopeSet.add(entry.toString());
                }
            }
        }

        return scopeSet;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "unknown";
    }
}
