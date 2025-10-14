package vecera.projekt.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfigurace Spring Security pro aplikaci PojištěníApp.
 * <p>
 * Hlavní zodpovědnosti:
 * <ul>
 *   <li>Definuje autentizaci přes {@link UserDetailsService} a zvolený {@link PasswordEncoder}
 *       (delegující encoder podporuje formáty jako {@code {bcrypt}}, {@code {noop}}, ...).</li>
 *   <li>Nastavuje autorizaci HTTP požadavků – veřejné cesty, role pro chráněné sekce
 *       (ROLE_USER/ROLE_ADMIN), a jemnější pravidla pro detailové stránky.</li>
 *   <li>Zapíná metodu @PreAuthorize (viz {@link EnableMethodSecurity}) – lze volat např.
 *       {@code @PreAuthorize("hasRole('ADMIN')")} nebo s vlastním beanem {@code @sec}.</li>
 *   <li>Konfiguruje přihlášení (custom /login), odhlášení, chování při 401/403 a správu session.</li>
 *   <li>Volitelně aktivuje „remember-me“ (pamatování přihlášení) – stačí mít checkbox
 *       s name="remember-me" ve formuláři.</li>
 * </ul>
 * Bezpečnostní poznámka: CSRf je pro form-login defaultně zapnuté; Thymeleaf šablony proto
 * vkládají CSRF token (hidden input) do formulářů.
 */

@Configuration
@EnableMethodSecurity // umožní @PreAuthorize s tvým beanem "sec"
public class SecurityConfig {

    /**
     * DelegatingPasswordEncoder: umí {noop}, {bcrypt}, … — hodí se při migraci hesel.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Autentizace přes tvoji UserDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // statická aktiva (lepší matcher než ruční /css/** apod.)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // veřejné
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password", "/o-aplikaci",
                                "/css/**", "/js/**", "/img/**", "/webjars/**", "/error").permitAll()

                        // USER i ADMIN smí číst DETAIL pojistky/pojištěného
                        .requestMatchers(HttpMethod.GET, "/pojistky/detail/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pojistenci/detail/**").hasAnyRole("USER","ADMIN")

                        // ADMIN sekce – vše ostatní pod /pojistenci/** + /pojistky/**
                        .requestMatchers("/pojistenci/**", "/pojistky/**").hasRole("ADMIN")

                        // Události – pro oba (detailní kontrola přes @PreAuthorize)
                        .requestMatchers("/udalosti/**").hasAnyRole("USER","ADMIN")

                        // zbytek vyžaduje přihlášení
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")     // POST /login zpracuje Spring Security
                        .failureUrl("/login?error")       // špatné přihlášení → zůstaň na loginu s ?error
                        .defaultSuccessUrl("/", true)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // místo „suchého“ 403: pošleme uživatele na login s paramem, který si v login.html zobrazíš
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/login"))         // nepřihlášený
                        .accessDeniedHandler((req, res, e) -> res.sendRedirect("/login?denied"))       // přihlášený, ale bez práv
                )

                // standardní ochrana session (doporučené pro form-login)
                .sessionManagement(sm -> sm
                        .sessionFixation().migrateSession()
                )

                // volitelné „zapamatuj si mě“ (přidej checkbox <input name="remember-me"> do loginu)
                .rememberMe(rm -> rm
                        .key("pojisteni-remember-me-key") // libovolný stabilní klíč
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                )
        ;

        return http.build();
    }
}
