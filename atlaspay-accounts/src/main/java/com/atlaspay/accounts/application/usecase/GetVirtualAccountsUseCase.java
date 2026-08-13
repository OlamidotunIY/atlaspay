package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.dto.VirtualAccountDto;
import com.atlaspay.accounts.application.query.GetVirtualAccountsQuery;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetVirtualAccountsUseCase extends BaseUseCase<GetVirtualAccountsQuery, List<VirtualAccountDto>> {

    private final VirtualAccountDomainRepository repository;

    @Override
    public List<VirtualAccountDto> execute(GetVirtualAccountsQuery query) {
        List<VirtualAccount> accounts = repository.findByOwnerId(query.ownerId());
        
        return accounts.stream()
                .map(account -> new VirtualAccountDto(
                        account.getId().value(),
                        account.getOwnerId(),
                        account.getAccountName(),
                        account.getNuban() != null ? account.getNuban().value() : null,
                        account.getBankName(),
                        account.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
