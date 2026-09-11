package code.filipesz.springdeliveryengine.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login/**").permitAll()

                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/", "/index.html", "/static/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/{id}/pay").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/{id}/paybycash").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/{id}/track").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/{id}/rate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/codes/{code}").permitAll()

                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/ws-tracking/**").permitAll()
                        .requestMatchers("/api/db/couriers/location").authenticated()
                        .requestMatchers("/api/orders/{id}/on-the-way").authenticated()
                        .requestMatchers("/api/orders/{id}/delivered").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}