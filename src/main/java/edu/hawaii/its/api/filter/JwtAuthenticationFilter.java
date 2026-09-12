package edu.hawaii.its.api.filter;

import edu.hawaii.its.api.service.JwtRoleConverter;
import edu.hawaii.its.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtRoleConverter jwtRoleConverter;

    public JwtAuthenticationFilter(JwtService jwtService, JwtRoleConverter jwtRoleConverter) {
        this.jwtService = jwtService;
        this.jwtRoleConverter = jwtRoleConverter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        // Authenticate and validate the token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.isTokenValid(jwt)) {
            // Map the roles claim onto Spring Security authorities, which is where the
            // ROLE_ prefix gets applied. The token carries plain role names.
            List<String> roles = jwtService.extractRoles(jwt);
            List<GrantedAuthority> authorities = jwtRoleConverter.convert(roles);

            UserDetails userDetails = new User(username, "", authorities);

            // Create the authentication token.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            // Set additional details like IP address.
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the authentication object in the SecurityContext.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        // Pass request to the next filter in the chain.
        filterChain.doFilter(request, response);
    }
}