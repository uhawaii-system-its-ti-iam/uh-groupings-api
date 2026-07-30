package edu.hawaii.its.api.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.SecurityContextRoleService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestJwtAuthenticationFilter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityContextRoleService securityContextRoleService;

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${groupings.api.test.admin_user}")
    private String TEST_ADMIN;

    private final List<String> ADMIN_ROLES = List.of("UH", "ADMIN");
    private final long TEST_EXPIRATION_TIME = 100000L; // 100 seconds

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void validTokenAuthenticatedTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, ADMIN_ROLES, TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set for valid token");
        assertTrue(auth.getPrincipal() instanceof UserDetails);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        assertEquals(TEST_ADMIN, userDetails.getUsername());

        // The token carries plain role names; the filter is what turns them into
        // ROLE_-prefixed authorities.
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_UH")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    public void prefixedRolesFromAnOlderUiAuthenticateTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, List.of("ROLE_UH", "ROLE_ADMIN"), TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set for a token with prefixed roles");

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_UH")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROLE_ADMIN")));
    }

    /**
     * The regression this addresses: the UI sends plain role names, and an admin must
     * come out of the filter holding the ADMIN role, which is what gates the admin table.
     * Before authority mapping existed, "ADMIN" became an ADMIN authority rather than
     * ROLE_ADMIN, isCurrentUserAdmin() returned false, and the admin table broke.
     */
    @Test
    public void plainRolesFromTheUiGrantTheAdminRoleTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, ADMIN_ROLES, TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertTrue(securityContextRoleService.isCurrentUserAdmin(), "An ADMIN token should grant the admin role");
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void plainRolesFromTheUiGrantTheOwnerRoleTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, List.of("UH", "OWNER"), TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertTrue(securityContextRoleService.isCurrentUserOwner(), "An OWNER token should grant the owner role");
        assertFalse(securityContextRoleService.isCurrentUserAdmin());
    }

    @Test
    public void unknownRoleDoesNotGrantAdminTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, List.of("UH", "SUPERUSER"), TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertFalse(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void unknownRoleIsDiscardedTest() throws Exception {
        String validToken = generateToken(TEST_ADMIN, List.of("UH", "SUPERUSER"), TEST_EXPIRATION_TIME);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set for a token with a known role");

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_UH")));
    }

    @Test
    public void missingAuthorizationTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should not be set when Authorization header is missing");
    }

    @Test
    public void malformedAuthorizationTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Malformed Token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should not be set when Authorization header is malformed");
    }

    @Test
    public void invalidTokenSignatureTest() {
        String badSecretKey = "badsecretkeybadsecretkeybadsecretkeybadsecretkey";
        String invalidToken = generateToken(TEST_ADMIN, ADMIN_ROLES, TEST_EXPIRATION_TIME, badSecretKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertThrows(SignatureException.class, () ->
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should not be set when token signature is invalid");
    }

    @Test
    public void expiredTokenTest() {
        String expiredToken = generateToken(TEST_ADMIN, ADMIN_ROLES, -1000L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertThrows(ExpiredJwtException.class, () ->
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should not be set when token is expired");
    }

    @Test
    public void emptyBearerTokenTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertThrows(IllegalArgumentException.class, () ->
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should not be set when Bearer token is empty");
    }

    @Test
    public void tokenWithoutRolesTest() {
        SecretKey signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        String tokenWithoutRoles = Jwts.builder()
                .subject(TEST_ADMIN)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION_TIME))
                .signWith(signInKey)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenWithoutRoles);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));

        // A signature-valid token with no roles claim authenticates the user, but grants
        // no authority, so every role check denies them. This previously threw a
        // NullPointerException out of the filter and surfaced as a 500.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set when the token is otherwise valid");
        assertTrue(auth.getAuthorities().isEmpty(), "A token with no roles claim should grant no authority");
    }

    // #######################################################
    // ################### HELPER FUNCTIONS ##################
    // #######################################################

    /**
     * Generate a JWT token with default secret key.
     */
    private String generateToken(String username, List<String> roles, long expirationTime) {
        return generateToken(username, roles, expirationTime, SECRET_KEY);
    }

    /**
     * Generate a JWT token with custom secret key.
     */
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
