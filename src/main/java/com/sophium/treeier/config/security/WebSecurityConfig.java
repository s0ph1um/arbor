package com.sophium.treeier.config.security;

import com.sophium.treeier.config.security.properties.CsrfProperties;
import com.sophium.treeier.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/actuator/**", "/login/**", "/oauth2/**").permitAll() // remove /api and uncomment below
                    .anyRequest().authenticated() // todo
//                .anyRequest().permitAll() // todo
            )
            .oauth2Login(oauth2 -> {
                oauth2.redirectionEndpoint(redirection ->
                    redirection.baseUri("/login/oauth2/code/google")
                );
            })
//            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//            .sessionManagement(session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // For API (react will send JWT tokens)
            .csrf(AbstractHttpConfigurer::disable)
            .cors(customizer -> customizer.configurationSource(corsConfigurationSource));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://accounts.google.com");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3003"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository(CsrfProperties csrfProperties) {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = new CookieCsrfTokenRepository();

        cookieCsrfTokenRepository.setCookieName(csrfProperties.getToken());
        cookieCsrfTokenRepository.setHeaderName(csrfProperties.getToken());
        cookieCsrfTokenRepository.setCookieCustomizer(cookieBuilder ->
            cookieBuilder
                .domain(csrfProperties.getDomain())
                .httpOnly(false)
                .secure(true)
                .sameSite("Strict")
                .path("/")
        );

        return cookieCsrfTokenRepository;
    }
}
