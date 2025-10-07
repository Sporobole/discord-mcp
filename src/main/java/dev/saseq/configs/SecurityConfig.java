package dev.saseq.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${mcp.security.bearer-token:}")
    private String expectedBearerToken;

    /**
     * Configures HTTP security for the MCP server endpoints.
     * Protects /sse and /mcp/message endpoints with Bearer token authentication.
     * Allows unauthenticated access to health check endpoint.
     *
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain configured for the application
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/sse", "/mcp/message").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(bearerTokenFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates a filter that validates Bearer tokens in the Authorization header.
     *
     * @return Filter for Bearer token authentication
     */
    private Filter bearerTokenFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String requestUri = httpRequest.getRequestURI();

                // Skip authentication for health check
                if (requestUri.equals("/actuator/health")) {
                    chain.doFilter(request, response);
                    return;
                }

                // Only authenticate MCP endpoints
                if (requestUri.equals("/sse") || requestUri.equals("/mcp/message")) {
                    String authHeader = httpRequest.getHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        httpResponse.getWriter().write("Missing or invalid Authorization header");
                        return;
                    }

                    String token = authHeader.substring(7);

                    if (expectedBearerToken == null || expectedBearerToken.isEmpty()) {
                        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        httpResponse.getWriter().write("Server configuration error: Bearer token not configured");
                        return;
                    }

                    if (!token.equals(expectedBearerToken)) {
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.getWriter().write("Invalid Bearer token");
                        return;
                    }
                }

                chain.doFilter(request, response);
            }
        };
    }
}
