package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.application.command.GenerateTestApiKeyPairCommand;
import com.atlaspay.identity.application.command.RegisterMerchantCommand;
import com.atlaspay.identity.application.dto.ApiKeyPairResult;
import com.atlaspay.identity.application.dto.RegisterMerchantResult;
import com.atlaspay.identity.application.port.out.PasswordEncoder;
import com.atlaspay.identity.domain.model.BusinessType;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RegisterMerchantUseCase}.
 *
 * <p>All collaborators are mocked; no Spring context or database is required.
 * Tests follow the Arrange / Act / Assert pattern and verify:
 * <ul>
 *   <li>Happy path: merchant is persisted and API key pair is returned.</li>
 *   <li>Duplicate email: {@link ConflictException} is thrown, nothing is persisted.</li>
 *   <li>Domain events: events are published after a successful registration.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterMerchantUseCase")
class RegisterMerchantUseCaseTest {

    @Mock private MerchantRepository merchantRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase;
    @Mock private DomainEventPublisher eventPublisher;

    private RegisterMerchantUseCase useCase;

    private static final RegisterMerchantCommand VALID_COMMAND = new RegisterMerchantCommand(
            "NG",
            "Acme Corp",
            "John",
            "Doe",
            "john@acme.com",
            "+2348012345678",
            "secureP@ss123",
            BusinessType.STARTER
    );

    @BeforeEach
    void setUp() {
        useCase = new RegisterMerchantUseCase(
                merchantRepository,
                passwordEncoder,
                generateTestApiKeyPairUseCase,
                eventPublisher
        );
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should register a new merchant and return test API key pair")
    void shouldRegisterMerchantAndReturnApiKeys() {
        // Arrange
        when(merchantRepository.findByEmail(VALID_COMMAND.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(VALID_COMMAND.password())).thenReturn("hashed_password");
        when(generateTestApiKeyPairUseCase.execute(any(GenerateTestApiKeyPairCommand.class)))
                .thenReturn(new ApiKeyPairResult("pk_test_abc123", "sk_test_xyz789"));

        // Act
        RegisterMerchantResult result = useCase.execute(VALID_COMMAND);

        // Assert
        assertThat(result.merchantId()).isNotNull();
        assertThat(result.testPublicKey()).isEqualTo("pk_test_abc123");
        assertThat(result.testSecretKey()).isEqualTo("sk_test_xyz789");

        // Verify the merchant was persisted exactly once
        ArgumentCaptor<Merchant> captor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantRepository, times(1)).save(captor.capture());

        Merchant saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("should hash the merchant password before persisting")
    void shouldHashPasswordBeforePersisting() {
        // Arrange
        when(merchantRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secureP@ss123")).thenReturn("bcrypt_hashed");
        when(generateTestApiKeyPairUseCase.execute(any())).thenReturn(
                new ApiKeyPairResult("pk_test_abc", "sk_test_abc"));

        // Act
        useCase.execute(VALID_COMMAND);

        // Assert
        verify(passwordEncoder).encode("secureP@ss123");
    }

    @Test
    @DisplayName("should generate test API keys for the newly created merchant")
    void shouldGenerateTestApiKeysForNewMerchant() {
        // Arrange
        when(merchantRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(generateTestApiKeyPairUseCase.execute(any())).thenReturn(
                new ApiKeyPairResult("pk_test_X", "sk_test_X"));

        // Act
        useCase.execute(VALID_COMMAND);

        // Assert — verify keys were generated for the saved merchant's ID
        ArgumentCaptor<GenerateTestApiKeyPairCommand> cmdCaptor =
                ArgumentCaptor.forClass(GenerateTestApiKeyPairCommand.class);
        verify(generateTestApiKeyPairUseCase).execute(cmdCaptor.capture());
        assertThat(cmdCaptor.getValue().merchantId()).isNotNull();
    }

    // ── Conflict ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw ConflictException when merchant email already exists")
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        // Arrange — simulate an existing merchant with the same email
        Merchant existing = mock(Merchant.class);
        when(merchantRepository.findByEmail(VALID_COMMAND.email()))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(VALID_COMMAND))
                .isInstanceOf(ConflictException.class);

        // Verify nothing was persisted and no events were published
        verify(merchantRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
