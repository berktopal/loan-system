package com.loanoriginationsystem.loan_system.controller;

import com.loanoriginationsystem.loan_system.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // React'ten gelecek istekler için
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            // Spring Security'nin yöneticisi, gönderilen kullanıcı adı ve şifreyi kontrol eder
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new RuntimeException("Hatalı kullanıcı adı veya şifre!");
        }

        // Eğer şifre doğruysa, kullanıcı bilgilerini al ve şifreli bir bilet (Token) üret
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}

// Dışarıdan gelecek JSON isteklerini karşılayacak sınıflar
@Data
class AuthRequest {
    private String username;
    private String password;
}

@Data
class AuthResponse {
    private final String token;
}