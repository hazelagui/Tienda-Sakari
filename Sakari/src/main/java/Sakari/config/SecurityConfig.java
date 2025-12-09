package Sakari.config;

import Sakari.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ============ RECURSOS ESTÁTICOS (Públicos) ============
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico", "/webjars/**").permitAll()
                
                // ============ PÁGINAS PÚBLICAS ============
                .requestMatchers("/", "/inicio", "/home").permitAll()
                .requestMatchers("/productos", "/productos/**").permitAll()
                .requestMatchers("/contacto").permitAll()
                .requestMatchers("/quienes-somos", "/personalizaciones").permitAll()
                .requestMatchers("/politicas-privacidad", "/terminos-condiciones").permitAll()
                .requestMatchers("/error", "/error/**").permitAll()
                
                // ============ AUTENTICACIÓN (Públicas) ============
                .requestMatchers("/login", "/registro").permitAll()
                .requestMatchers("/recuperar-password", "/reset-password").permitAll()
                
                // ============ PANEL DE ADMINISTRACIÓN (Solo ADMIN) ============
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // ============ RUTAS DE USUARIO AUTENTICADO ============
                .requestMatchers("/carrito/**").authenticated()
                .requestMatchers("/pedidos/**").authenticated()
                .requestMatchers("/perfil/**").authenticated()
                
                // ============ CUALQUIER OTRA RUTA ============
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .rememberMe(remember -> remember
                .key("sakari-remember-me-key")
                .tokenValiditySeconds(86400) // 24 horas
                .userDetailsService(userDetailsService)
            );

        return http.build();
    }
}
