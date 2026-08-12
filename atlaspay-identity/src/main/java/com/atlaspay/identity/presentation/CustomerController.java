package com.atlaspay.identity.presentation;

import com.atlaspay.identity.application.command.CreateCustomerCommand;
import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.dto.CreateCustomerResult;
import com.atlaspay.identity.application.query.GetCustomerQuery;
import com.atlaspay.identity.application.query.ListCustomersQuery;
import com.atlaspay.identity.application.usecase.CreateCustomerUseCase;
import com.atlaspay.identity.application.usecase.GetCustomerUseCase;
import com.atlaspay.identity.application.usecase.ListCustomersUseCase;
import com.atlaspay.identity.presentation.dto.CreateCustomerRequest;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase, 
                              GetCustomerUseCase getCustomerUseCase,
                              ListCustomersUseCase listCustomersUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a customer", description = "Creates a new customer for a merchant")
    public ResponseEntity<CreateCustomerResult> create(@Valid @RequestBody CreateCustomerRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        
        CreateCustomerCommand command = new CreateCustomerCommand(
                new MerchantId(merchantIdStr),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.metadata()
        );
        CreateCustomerResult result = createCustomerUseCase.execute(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get a customer", description = "Retrieves a customer by ID")
    public ResponseEntity<CustomerDto> get(@PathVariable String customerId, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetCustomerQuery query = new GetCustomerQuery(new MerchantId(merchantIdStr), new CustomerId(customerId));
        CustomerDto result = getCustomerUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    @Operation(summary = "List customers", description = "Retrieves a paginated list of customers")
    public ResponseEntity<PageResult<CustomerDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        ListCustomersQuery query = new ListCustomersQuery(new MerchantId(merchantIdStr), page, size, null);
        PageResult<CustomerDto> result = listCustomersUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
