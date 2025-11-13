package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${groupings.api.test.admin_user}")
    private String TEST_ADMIN;

    private final List<String> TEST_ROLES = List.of("ROLE_UH", "ROLE_ADMIN");
    private final long TEST_EXPIRATION_TIME = 10000; // 10 sec

    private String validToken;

    @BeforeEach
    public void setup() {
        this.validToken = generateToken(TEST_ADMIN, TEST_ROLES, TEST_EXPIRATION_TIME, SECRET_KEY);
    }

    @Test
    public void extractUsernameTest() {
        assertEquals(TEST_ADMIN, jwtService.extractUsername(validToken));
    }

    @Test
    void isTokenValidTest() {
        assertTrue(jwtService.isTokenValid(validToken));
    }

    @Test
    public void extractRolesTest() {
        assertEquals(TEST_ROLES, jwtService.extractRoles(validToken));
    }

    @Test
    public void tokenExpiredTest() {
        String expiredToken = generateToken(TEST_ADMIN, TEST_ROLES, -1000L, SECRET_KEY);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(expiredToken);
        });
    }

    @Test
    public void invalidSignatureTest() {
        String badSecretKey = "SOqA7uqcC8sF+HY8QNHVtVsxS0M7EIJNEWVGAEhbN1I=";
        String badToken = generateToken(TEST_ADMIN, TEST_ROLES, TEST_EXPIRATION_TIME, badSecretKey);

        assertThrows(SignatureException.class, () -> {
            jwtService.extractUsername(badToken);
        });

        assertThrows(SignatureException.class, () -> {
            jwtService.isTokenValid(badToken);
        });
    }

    @Test
    public void malformedTokenTest() {
        String malformedToken = "not.a.real.token";

        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUsername(malformedToken);
        });
    }

    // #######################################################
    // ################### HELPER FUNCTIONS ##################
    // #######################################################
    private String generateToken(String username, List<String> roles, long expirationTime, String secretKey) {

        SecretKey signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signInKey)
                .compact();
    }
}