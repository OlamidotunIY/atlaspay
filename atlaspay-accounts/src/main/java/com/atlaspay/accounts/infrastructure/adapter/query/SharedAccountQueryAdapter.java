package com.atlaspay.accounts.infrastructure.adapter.query;

import com.atlaspay.accounts.infrastructure.repository.JpaVirtualAccountRepository;
import com.atlaspay.shared.money.CurrencyCode;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import com.atlaspay.shared.port.out.AccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SharedAccountQueryAdapter implements AccountQueryPort {

    private final JpaVirtualAccountRepository repository;

    @Override
    public Optional<AccountDetailsDto> findAccountDetails(Long accountId) {
        return repository.findById(accountId)
                .map(entity -> new AccountDetailsDto(
                        entity.getId(),
                        entity.getIntegration(),
                        CurrencyCode.valueOf(entity.getCurrency()),
                        entity.getStatus()
                ));
    }

    @Override
    public List<AccountDetailsDto> findAccountsByIntegration(Long integration) {
        return repository.findByIntegration(integration).stream()
                .map(entity -> new AccountDetailsDto(
                        entity.getId(),
                        entity.getIntegration(),
                        CurrencyCode.valueOf(entity.getCurrency()),
                        entity.getStatus()
                ))
                .toList();
    }
}
