package com.atlaspay.notifications.application.usecase;

import com.atlaspay.notifications.application.port.EmailSenderPort;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

@Service
public class SendVerificationEmailUseCase extends BaseUseCase<SendVerificationEmailUseCase.Input, Void> {

    private final EmailSenderPort emailSenderPort;

    public SendVerificationEmailUseCase(EmailSenderPort emailSenderPort) {
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    public Void execute(Input input) {
        emailSenderPort.sendVerificationEmail(input.email(), input.verificationCode());
        return null;
    }

    public record Input(String email, String verificationCode) {}
}
