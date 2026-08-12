package com.atlaspay.identity.presentation.rest;

import com.atlaspay.identity.application.command.RegenerateApiKeyCommand;
import com.atlaspay.identity.application.command.RevokeApiKeyCommand;
import com.atlaspay.identity.application.dto.ApiKeyDto;
import com.atlaspay.identity.application.query.ListApiKeysQuery;
import com.atlaspay.identity.application.usecase.ListApiKeysUseCase;
import com.atlaspay.identity.application.usecase.RegenerateApiKeyUseCase;
import com.atlaspay.identity.application.usecase.RevokeApiKeyUseCase;
import com.atlaspay.identity.domain.model.ApiEnvironment;
import com.atlaspay.identity.domain.model.KeyType;
import com.atlaspay.identity.presentation.rest.dto.RegenerateApiKeyRequest;
import com.atlaspay.shared.domain.id.ApiKeyId;
import com.atlaspay.shared.domain.id.MerchantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/keys")
@Tag(name = "API Keys", description = "Merchant API key management")
public class ApiKeyController {

    private final ListApiKeysUseCase listApiKeysUseCase;
    private final RegenerateApiKeyUseCase regenerateApiKeyUseCase;
    private final RevokeApiKeyUseCase revokeApiKeyUseCase;

    public ApiKeyController(ListApiKeysUseCase listApiKeysUseCase,
                            RegenerateApiKeyUseCase regenerateApiKeyUseCase,
                            RevokeApiKeyUseCase revokeApiKeyUseCase) {
        this.listApiKeysUseCase = listApiKeysUseCase;
        this.regenerateApiKeyUseCase = regenerateApiKeyUseCase;
        this.revokeApiKeyUseCase = revokeApiKeyUseCase;
    }

    @GetMapping
    @Operation(summary = "List all API keys", description = "Retrieves all active and revoked API keys for the authenticated merchant")
    public ResponseEntity<Map<String, List<ApiKeyDto>>> listKeys(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        ListApiKeysQuery query = new ListApiKeysQuery(new MerchantId(merchantIdStr));
        List<ApiKeyDto> keys = listApiKeysUseCase.execute(query);
        return ResponseEntity.ok(Map.of("keys", keys));
    }

    @PostMapping("/regenerate")
    @Operation(summary = "Regenerate an API key", description = "Revokes the active key of the specified type and generates a new one")
    public ResponseEntity<Map<String, String>> regenerateKey(@Valid @RequestBody RegenerateApiKeyRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        RegenerateApiKeyCommand command = new RegenerateApiKeyCommand(
                new MerchantId(merchantIdStr),
                KeyType.valueOf(request.keyType()),
                ApiEnvironment.valueOf(request.environment())
        );
        String rawKey = regenerateApiKeyUseCase.execute(command);
        return ResponseEntity.ok(Map.of("rawKey", rawKey));
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke an API key", description = "Revokes a specific API key")
    public ResponseEntity<Map<String, Object>> revokeKey(@PathVariable String keyId, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(
                new MerchantId(merchantIdStr),
                new ApiKeyId(keyId)
        );
        revokeApiKeyUseCase.execute(command);
        return ResponseEntity.ok(Map.of("keyId", keyId, "active", false));
    }
}
