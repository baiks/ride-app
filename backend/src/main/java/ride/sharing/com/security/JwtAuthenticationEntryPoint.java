package ride.sharing.com.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ride.sharing.com.dtos.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom authentication entry point for handling authentication failures
 * This is triggered when an unauthenticated user tries to access a protected resource
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Unauthorized access attempt to: {} from IP: {}", 
                request.getRequestURI(), 
                request.getRemoteAddr());

        // Determine the specific error message based on the exception or request attributes
        String message = "Authentication required. Please provide a valid token";
        String error = "Unauthorized";
        
        // Check if there was a JWT-specific error
        String jwtError = (String) request.getAttribute("jwt_error");
        if (jwtError != null) {
            message = jwtError;
            if (jwtError.contains("expired")) {
                error = "Token Expired";
            } else if (jwtError.contains("invalid") || jwtError.contains("malformed")) {
                error = "Invalid Token";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}