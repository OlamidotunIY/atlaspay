package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.usecase.BaseUseCase;

import com.atlaspay.identity.application.dto.CreateCustomerResult;
import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.identity.domain.model.Customer;
import com.atlaspay.identity.domain.repository.CustomerRepository;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.exception.ConflictException;

public class CreateCustomerUseCase extends BaseUseCase<CreateCustomerCommand, CreateCustomerResult> {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateCustomerResult execute(CreateCustomerCommand command) {
        if (customerRepository.findByMerchantIdAndEmail(command.merchantId(), command.email()).isPresent()) {
            throw new ConflictException(IdentityErrorCode.CUSTOMER_EMAIL_ALREADY_EXISTS, "Customer with this email already exists for this merchant");
        }

        Customer customer = new Customer(
            CustomerId.generate(),
            command.merchantId(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            new PhoneNumber(command.phone()),
            command.metadata()
        );

        customerRepository.save(customer);

        publishEvents(customer, eventPublisher);

        return new CreateCustomerResult(customer.getId());
    }
}
