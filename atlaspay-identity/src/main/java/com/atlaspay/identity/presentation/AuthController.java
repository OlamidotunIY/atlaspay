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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.AuthorizationException;

@RestController
@RequestMapping("/api/v1/auth")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Merchant authentication and authorization")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
    @io.swagger.v3.oas.annotations.Operation(summary = "Merchant Login")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid email or password")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Attempting login for email: {}", request.email());
        Optional<Merchant> merchantOpt = merchantRepository.findByEmail(request.email());

        if (merchantOpt.isEmpty()) {
            log.warn("Login failed: Merchant with email {} not found", request.email());
            throw new AuthorizationException(IdentityErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        Merchant merchant = merchantOpt.get();

        if (!passwordEncoder.matches(request.password(), merchant.getHashedPassword())) {
            log.warn("Login failed: Incorrect password for email {}", request.email());
            throw new AuthorizationException(IdentityErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        String token = jwtService.generateToken(merchant.getId() != null ? String.valueOf(merchant.getId()) : null);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
