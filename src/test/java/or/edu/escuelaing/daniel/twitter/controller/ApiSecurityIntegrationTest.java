package or.edu.escuelaing.daniel.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithAnonymousUser
    void getPostsShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void postWithoutTokenShouldReturnUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(new PostRequest("hello"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postWithWriteScopeShouldReturnCreated() throws Exception {
        String body = objectMapper.writeValueAsString(new PostRequest("secure post"));

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("auth0|user-1").claim("name", "Test User"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_write:posts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("secure post"))
                .andExpect(jsonPath("$.authorId").value("auth0|user-1"));
    }

    @Test
    void postWithInvalidBodyShouldReturnBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(new PostRequest("x".repeat(141)));

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("auth0|user-1").claim("name", "Test User"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_write:posts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithMissingWriteScopeShouldReturnForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(new PostRequest("hello"));

        mockMvc.perform(post("/api/posts")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("auth0|user-1"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_read:posts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void meWithReadProfileScopeShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|user-1")
                                        .claim("name", "Test User")
                                        .claim("email", "user@example.com")
                                        .claim("scope", "read:profile write:posts"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_read:profile"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("auth0|user-1"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void meWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    private record PostRequest(String content) {
    }
}
