package chat.halixe.chat.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-32bytes-ok!".getBytes());

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    @Test
    void validateReturnsCorrectUser() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        String token = buildToken(userId, orgId, futureDate(3600));

        AuthenticatedUser user = jwtUtil.validate(token);

        assertThat(user.userId()).isEqualTo(userId);
        assertThat(user.organisationId()).isEqualTo(orgId);
    }

    @Test
    void validateThrowsOnExpiredToken() {
        String token = buildToken(UUID.randomUUID(), UUID.randomUUID(), pastDate(3600));

        assertThatThrownBy(() -> jwtUtil.validate(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateThrowsOnTamperedSignature() {
        String token = buildToken(UUID.randomUUID(), UUID.randomUUID(), futureDate(3600));
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsig";

        assertThatThrownBy(() -> jwtUtil.validate(tampered))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateThrowsOnTokenSignedWithDifferentSecret() {
        String otherSecret = Base64.getEncoder()
                .encodeToString("other-secret-key-totally-32bytes".getBytes());
        SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(otherSecret));

        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("org", UUID.randomUUID().toString())
                .expiration(futureDate(3600))
                .signWith(otherKey)
                .compact();

        assertThatThrownBy(() -> jwtUtil.validate(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void authenticatedUserToStringReturnsUserId() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, UUID.randomUUID());
        assertThat(user.toString()).isEqualTo(userId.toString());
    }

    private String buildToken(UUID userId, UUID orgId, Date expiry) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(userId.toString())
                .claim("org", orgId.toString())
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private Date futureDate(int seconds) {
        return new Date(System.currentTimeMillis() + seconds * 1000L);
    }

    private Date pastDate(int seconds) {
        return new Date(System.currentTimeMillis() - seconds * 1000L);
    }
}
