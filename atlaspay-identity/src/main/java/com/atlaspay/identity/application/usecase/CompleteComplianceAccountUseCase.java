package com.atlaspay.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlaspay.identity.application.command.CompleteComplianceAccountCommand;
import com.atlaspay.identity.application.port.AccountNameResolutionPort;
import com.atlaspay.identity.domain.model.ComplianceStatus;
import com.atlaspay.identity.domain.model.Merchant;
import com.atlaspay.identity.domain.repository.MerchantRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Service
public class CompleteComplianceAccountUseCase extends BaseUseCase<CompleteComplianceAccountCommand, ComplianceStatus> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceAccountUseCase.class);


    private final MerchantRepository merchantRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceAccountUseCase(
            MerchantRepository merchantRepository,
            AccountNameResolutionPort accountNameResolutionPort,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ComplianceStatus execute(CompleteComplianceAccountCommand command) {
        log.info("Executing CompleteComplianceAccountUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        String accountName = accountNameResolutionPort.resolve(command.settlementBankCode(), command.settlementAccountNumber());

        merchant.updateComplianceAccount(
            command.settlementBankCode(),
            command.settlementAccountNumber(),
            accountName
        );

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);

        return merchant.getComplianceStatus();
    }
}



