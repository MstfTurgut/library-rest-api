package com.mstftrgt.libraryrestapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class LibrarySecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET,"/books").hasAnyRole("STUDENT", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/books/{id}").hasAnyRole("STUDENT", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/books").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/books/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/books/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/students/borrowedBooks/{username}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/books/{id}/checkout").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/me/borrowedBooks").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/me/borrowedBooks/{id}/return").hasRole("STUDENT")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {

        return new JdbcUserDetailsManager(dataSource);
    }
}
