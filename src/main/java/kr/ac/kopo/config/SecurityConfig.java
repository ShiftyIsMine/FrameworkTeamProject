package kr.ac.kopo.config;


import kr.ac.kopo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Public access
                        .requestMatchers("/", "/css/**", "/js/**", "/imgs/**", "/images/**",
                                "/login", "/register", "/api/songs", "/api/categories",
                                "/api/artists", "/api/genres", "/api/songs/random/**",
                                "/api/songs/latest", "/h2-console/**","/add-song","/api/songs/**","/api/**").permitAll()

                        // Admin only access
                        .requestMatchers("/admin/**","/api/admin/**","/api/spotify/search-spotify").hasRole("ADMIN")

                        // Authenticated user access
                        .requestMatchers("/inner1", "/inner2", "/profile/**").hasAnyRole("USER", "ADMIN")

                        // All other requests need authentication
                        .anyRequest().permitAll()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")  // ✅ CSRF 예외
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                )
                .headers(headers -> headers.frameOptions().disable()) // H2 Console용
                .userDetailsService(userDetailsService);

        return http.build();
    }
}