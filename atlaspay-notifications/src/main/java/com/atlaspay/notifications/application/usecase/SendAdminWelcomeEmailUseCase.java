package com.atlaspay.notifications.application.usecase;

import com.atlaspay.notifications.application.port.EmailSenderPort;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

@Service
public class SendAdminWelcomeEmailUseCase extends BaseUseCase<SendAdminWelcomeEmailUseCase.Input, Void> {

    private final EmailSenderPort emailSenderPort;

    public SendAdminWelcomeEmailUseCase(EmailSenderPort emailSenderPort) {
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    public Void execute(Input input) {
        emailSenderPort.sendAdminWelcomeEmail(input.email(), input.temporaryPassword());
        return null;
    }

    public record Input(String email, String temporaryPassword) {}
}
