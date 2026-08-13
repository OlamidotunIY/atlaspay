package com.atlaspay.accounts.application.usecase;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.port.out.VirtualAccountQueryService;
import com.atlaspay.accounts.domain.model.OwnerType;
import com.atlaspay.accounts.domain.model.VirtualAccount;
import com.atlaspay.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IssueVirtualAccountUseCaseTest {

    private VirtualAccountDomainRepository repository;
    private VirtualAccountQueryService queryService;
    private DomainEventPublisher eventPublisher;
    private IssueVirtualAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(VirtualAccountDomainRepository.class);
        queryService = mock(VirtualAccountQueryService.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new IssueVirtualAccountUseCase(repository, queryService, eventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenMerchantHasTwoAccounts() {
        when(queryService.countByOwnerId("mer_1")).thenReturn(2L);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand("idem1", "mer_1", OwnerType.MERCHANT, "Wema");
        
        assertThrows(BusinessRuleException.class, () -> useCase.execute(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenMerchantHasDuplicateBank() {
        when(queryService.countByOwnerId("mer_1")).thenReturn(1L);
        when(queryService.existsByOwnerIdAndBankName("mer_1", "Wema")).thenReturn(true);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand("idem2", "mer_1", OwnerType.MERCHANT, "Wema");
        
        assertThrows(BusinessRuleException.class, () -> useCase.execute(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSaveAccountWhenValid() {
        when(queryService.countByOwnerId("mer_1")).thenReturn(0L);
        when(queryService.existsByOwnerIdAndBankName(any(), any())).thenReturn(false);
        when(repository.save(any(VirtualAccount.class))).thenAnswer(i -> i.getArguments()[0]);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand("idem3", "mer_1", OwnerType.MERCHANT, "Zenith");
        
        useCase.execute(cmd);
        
        verify(repository, times(1)).save(any(VirtualAccount.class));
    }
}
