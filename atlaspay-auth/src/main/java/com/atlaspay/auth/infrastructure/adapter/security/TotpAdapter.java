package com.atlaspay.auth.infrastructure.adapter.security;

import com.atlaspay.auth.application.port.out.TotpServicePort;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Component;

@Component
public class TotpAdapter implements TotpServicePort {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    @Override
    public String generateSecret() {
        return secretGenerator.generate();
    }

    @Override
    public String generateUri(String secret, String accountName) {
        QrData data = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer("AtlasPay")
                .build();
        return data.getUri();
    }

    @Override
    public boolean verify(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
