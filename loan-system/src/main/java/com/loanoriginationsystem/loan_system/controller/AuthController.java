package com.loanoriginationsystem.loan_system.controller;

import com.loanoriginationsystem.loan_system.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            // Spring Security kullanıcı adı ve şifreyi doğrular
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(userDetails)));
        } catch (AuthenticationException e) {
            // 500 yerine 401; hangi alanın yanlış olduğu söylenmez
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Hatalı kullanıcı adı veya şifre.");
        }
    }

    public record AuthRequest(
            @NotBlank(message = "Kullanıcı adı zorunludur.") String username,
            @NotBlank(message = "Şifre zorunludur.") String password) {
    }

    public record AuthResponse(String token) {
    }
}
