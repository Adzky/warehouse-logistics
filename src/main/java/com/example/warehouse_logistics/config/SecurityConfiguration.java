package com.example.warehouse_logistics.config;

import com.example.warehouse_logistics.repository.WarehouseUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Locale;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/receipts/**", "/api/inventory/receive")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER", "RECEIVER")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/pick", "/api/orders/*/pack",
                        "/api/orders/*/ship")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER", "PICKER")
                .requestMatchers(HttpMethod.POST, "/api/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/receipts/**", "/inventory/receive")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER", "RECEIVER")
                .requestMatchers(HttpMethod.POST, "/orders/*/pick", "/orders/*/pack", "/orders/*/ship")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER", "PICKER")
                .requestMatchers(HttpMethod.POST, "/orders/*/cancel", "/orders")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/inventory/**", "/warehouses/**", "/products/**",
                        "/suppliers/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/**")
                    .hasAnyRole("ADMINISTRATOR", "WAREHOUSE_MANAGER")
                .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(WarehouseUserRepository users) {
        return username -> users.findByUsername(username.toLowerCase(Locale.ROOT))
                .map(account -> User.withUsername(account.getUsername())
                        .password(account.getPasswordHash())
                        .roles(account.getRole().name())
                        .disabled(!account.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
