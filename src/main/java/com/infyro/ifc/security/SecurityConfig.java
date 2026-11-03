package com.infyro.ifc.security;

import tools.jackson.databind.ObjectMapper;
import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.api.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    @Value("${app.cors.allowed-origin:http://localhost:5173}")
    private String frontendOrigin;

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    @Bean public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(frontendOrigin));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Set-Cookie"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
    @Bean public AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper mapper) {
        return (request,response,exception) -> writeError(mapper,response,401,"UNAUTHORIZED",
                "Authentication is required");
    }
    @Bean public AccessDeniedHandler apiAccessDeniedHandler(ObjectMapper mapper) {
        return (request,response,exception) -> writeError(mapper,response,403,"FORBIDDEN",
                "You do not have permission to access this resource");
    }
    private void writeError(ObjectMapper mapper,jakarta.servlet.http.HttpServletResponse response,
                            int status,String code,String message)throws IOException {
        response.setStatus(status); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),new ApiErrorResponse(status,code,message,List.of()));
    }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, JWTFilter jwtFilter,
            AuthenticationEntryPoint entryPoint, AccessDeniedHandler deniedHandler)throws Exception {
        return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Cookie-authenticated refresh/logout routes enforce explicit double-submit CSRF validation.
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS,"/**").permitAll()
                        .requestMatchers(APIConstants.AUTH + APIConstants.Auth.LOGIN,
                                APIConstants.AUTH + APIConstants.Auth.SIGNUP,
                                APIConstants.AUTH + APIConstants.Auth.REFRESH,
                                APIConstants.AUTH + APIConstants.Auth.LOGOUT).permitAll()
                        .requestMatchers(APIConstants.USER + "/**").hasRole("USER")
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}