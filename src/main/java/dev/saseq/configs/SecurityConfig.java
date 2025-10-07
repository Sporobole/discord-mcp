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
                .cors(cors -> cors.disable()) // Disable CORS - allow all origins
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
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
                String method = httpRequest.getMethod();

                System.out.println("Bearer token filter: " + method + " " + requestUri);
                System.out.println("Authorization header: " + httpRequest.getHeader("Authorization"));

                    // Skip authentication for health check and error page
                    if (requestUri.equals("/actuator/health") || requestUri.equals("/error")) {
                        chain.doFilter(request, response);
                        return;
                    }

                    // Only authenticate SSE MCP endpoints
                    if (requestUri.equals("/sse") || requestUri.startsWith("/mcp/")) {
                    String authHeader = httpRequest.getHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        System.out.println("Authentication failed: Missing or invalid Authorization header");
                        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        httpResponse.setContentType("application/json");
                        httpResponse.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
                        return;
                    }

                    String token = authHeader.substring(7);

                    if (expectedBearerToken == null || expectedBearerToken.isEmpty()) {
                        System.out.println("Authentication failed: Bearer token not configured on server");
                        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        httpResponse.setContentType("application/json");
                        httpResponse.getWriter().write("{\"error\":\"Server configuration error: Bearer token not configured\"}");
                        return;
                    }

                    if (!token.equals(expectedBearerToken)) {
                        System.out.println("Authentication failed: Invalid Bearer token");
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.setContentType("application/json");
                        httpResponse.getWriter().write("{\"error\":\"Invalid Bearer token\"}");
                        return;
                    }

                    System.out.println("Authentication successful for " + requestUri);
                }

                chain.doFilter(request, response);
            }
        };
    }
}
