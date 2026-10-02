package com.loanoriginationsystem.loan_system.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class ApplicationConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    /**
     * Sisteme giriş yapabilecek kredi personeli.
     * Kimlik bilgileri koda gömülmez; ADMIN_USERNAME / ADMIN_PASSWORD ortam değişkenlerinden okunur.
     * ADMIN_PASSWORD verilmezse her açılışta rastgele bir şifre üretilip loga yazılır
     * (Spring Boot'un varsayılan kullanıcı davranışı gibi), böylece yerelde çalıştırmak kolay kalır.
     */
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.admin.username:admin}") String username,
            @Value("${app.security.admin.password:}") String password) {

        String effectivePassword = password;
        if (effectivePassword == null || effectivePassword.isBlank()) {
            byte[] bytes = new byte[18];
            new SecureRandom().nextBytes(bytes);
            effectivePassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            log.warn("ADMIN_PASSWORD tanımlı değil. Bu oturum için üretilen personel şifresi ({}): {}",
                    username, effectivePassword);
        } else if (effectivePassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD en az " + MIN_PASSWORD_LENGTH + " karakter olmalıdır.");
        }

        UserDetails admin = User.builder()
                .username(username)
                .password(passwordEncoder().encode(effectivePassword))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    // Şifreleri güvenli hale getiren motor
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Kimlik doğrulama yöneticisi
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
