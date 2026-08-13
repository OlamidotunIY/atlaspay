package com.atlaspay.identity.presentation;

import com.atlaspay.identity.infrastructure.adapter.security.JwtService;
import com.atlaspay.identity.application.port.PasswordEncoder;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
    ) {}
    
    public record LoginResponse(String token) {}

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(MerchantRepository merchantRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<Merchant> merchantOpt = merchantRepository.findByEmail(request.email());

        if (merchantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Merchant merchant = merchantOpt.get();

        if (!passwordEncoder.matches(request.password(), merchant.getHashedPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(merchant.getId() != null ? String.valueOf(merchant.getId()) : null);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
