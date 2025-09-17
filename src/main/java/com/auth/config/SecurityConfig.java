package com.auth.config;

import com.auth.security.JwtAuthFilter;
import com.auth.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwt)
            throws Exception {
            http.csrf().disable()
                    .authorizeHttpRequests(c -> c
                            .requestMatchers("api/token").permitAll()
                            .requestMatchers("/tasks/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(new JwtAuthFilter(jwt), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
            return http.build();

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager mgr = new JdbcUserDetailsManager(dataSource);

        mgr.setUsersByUsernameQuery("""
            select u.username, u.password, true as enabled
            from users u
            where u.username = ?
        """);

        mgr.setAuthoritiesByUsernameQuery("""
            select u.username, r.role as authority
            from users u
            join user_roles r on r.user_id = u.id
            where u.username = ?
        """);

        return mgr;
    }
}
