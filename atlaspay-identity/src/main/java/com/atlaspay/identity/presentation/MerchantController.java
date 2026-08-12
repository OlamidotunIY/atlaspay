package com.atlaspay.identity.presentation;

import com.atlaspay.identity.application.command.*;
import com.atlaspay.identity.application.dto.MerchantProfileDto;
import com.atlaspay.identity.application.dto.RegisterMerchantResult;
import com.atlaspay.identity.application.query.GetMerchantProfileQuery;
import com.atlaspay.identity.application.usecase.*;
import com.atlaspay.identity.domain.model.ComplianceStatus;
import com.atlaspay.identity.presentation.dto.*;
import com.atlaspay.shared.domain.id.MerchantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchants")
@Tag(name = "Merchants", description = "Merchant onboarding and profile management")
public class MerchantController {

    private final RegisterMerchantUseCase registerMerchantUseCase;
    private final GetMerchantProfileUseCase getMerchantProfileUseCase;
    private final VerifyMerchantEmailUseCase verifyMerchantEmailUseCase;
    private final CompleteComplianceProfileUseCase completeComplianceProfileUseCase;
    private final CompleteComplianceContactUseCase completeComplianceContactUseCase;
    private final CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase;
    private final CompleteComplianceAccountUseCase completeComplianceAccountUseCase;
    private final CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase;
    private final SubmitComplianceUseCase submitComplianceUseCase;

    public MerchantController(
            RegisterMerchantUseCase registerMerchantUseCase, 
            GetMerchantProfileUseCase getMerchantProfileUseCase,
            VerifyMerchantEmailUseCase verifyMerchantEmailUseCase,
            CompleteComplianceProfileUseCase completeComplianceProfileUseCase,
            CompleteComplianceContactUseCase completeComplianceContactUseCase,
            CompleteComplianceOwnerUseCase completeComplianceOwnerUseCase,
            CompleteComplianceAccountUseCase completeComplianceAccountUseCase,
            CompleteComplianceServiceAgreementUseCase completeComplianceServiceAgreementUseCase,
            SubmitComplianceUseCase submitComplianceUseCase) {
        this.registerMerchantUseCase = registerMerchantUseCase;
        this.getMerchantProfileUseCase = getMerchantProfileUseCase;
        this.verifyMerchantEmailUseCase = verifyMerchantEmailUseCase;
        this.completeComplianceProfileUseCase = completeComplianceProfileUseCase;
        this.completeComplianceContactUseCase = completeComplianceContactUseCase;
        this.completeComplianceOwnerUseCase = completeComplianceOwnerUseCase;
        this.completeComplianceAccountUseCase = completeComplianceAccountUseCase;
        this.completeComplianceServiceAgreementUseCase = completeComplianceServiceAgreementUseCase;
        this.submitComplianceUseCase = submitComplianceUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new merchant", description = "Creates a new merchant account and returns initial API keys")
    public ResponseEntity<RegisterMerchantResult> register(@Valid @RequestBody RegisterMerchantRequest request) {
        RegisterMerchantCommand command = new RegisterMerchantCommand(
                request.country(),
                request.businessName(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.password(),
                request.businessType()
        );
        RegisterMerchantResult result = registerMerchantUseCase.execute(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get merchant profile", description = "Retrieves the profile of the authenticated merchant")
    public ResponseEntity<MerchantProfileDto> getProfile(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetMerchantProfileQuery query = new GetMerchantProfileQuery(new MerchantId(merchantIdStr));
        MerchantProfileDto result = getMerchantProfileUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify merchant email")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@Valid @RequestBody VerifyMerchantEmailRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        VerifyMerchantEmailCommand command = new VerifyMerchantEmailCommand(
            new MerchantId(merchantIdStr),
            request.code()
        );
        verifyMerchantEmailUseCase.execute(command);
        return ResponseEntity.ok(Map.of("verified", true));
    }
    
    @GetMapping("/compliance")
    @Operation(summary = "Get compliance status")
    public ResponseEntity<ComplianceStatus> getComplianceStatus(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetMerchantProfileQuery query = new GetMerchantProfileQuery(new MerchantId(merchantIdStr));
        MerchantProfileDto result = getMerchantProfileUseCase.execute(query);
        return ResponseEntity.ok(ComplianceStatus.valueOf(result.kycStatus()));
    }

    @PutMapping("/compliance/profile")
    @Operation(summary = "Complete compliance profile step")
    public ResponseEntity<Void> completeProfile(@Valid @RequestBody CompleteComplianceProfileRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceProfileCommand command = new CompleteComplianceProfileCommand(
            new MerchantId(merchantIdStr),
            request.description(),
            request.staffSize(),
            request.industry(),
            request.category(),
            request.annualProjectedSalesVolume() != null ? request.annualProjectedSalesVolume().amount() : null,
            request.annualProjectedSalesVolume() != null ? request.annualProjectedSalesVolume().currency() : null
        );
        completeComplianceProfileUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/compliance/contact")
    @Operation(summary = "Complete compliance contact step")
    public ResponseEntity<Void> completeContact(@Valid @RequestBody CompleteComplianceContactRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceContactCommand command = new CompleteComplianceContactCommand(
            new MerchantId(merchantIdStr),
            request.supportEmail(),
            request.disputeEmail(),
            request.whatsappPhone(),
            request.whatsappName(),
            request.websiteUrl(),
            request.twitterHandle(),
            request.facebookUsername(),
            request.instagramHandle(),
            request.businessState(),
            request.businessLga(),
            request.businessCity(),
            request.businessStreet()
        );
        completeComplianceContactUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/compliance/owner")
    @Operation(summary = "Complete compliance owner step")
    public ResponseEntity<Void> completeOwner(@Valid @RequestBody CompleteComplianceOwnerRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceOwnerCommand command = new CompleteComplianceOwnerCommand(
            new MerchantId(merchantIdStr),
            request.bvn(),
            request.nin(),
            request.dateOfBirth(),
            request.address(),
            request.idType(),
            request.idNumber(),
            request.rcNumber()
        );
        completeComplianceOwnerUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/compliance/account")
    @Operation(summary = "Complete compliance account step")
    public ResponseEntity<Void> completeAccount(@Valid @RequestBody CompleteComplianceAccountRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceAccountCommand command = new CompleteComplianceAccountCommand(
            new MerchantId(merchantIdStr),
            request.bankCode(),
            request.accountNumber()
        );
        completeComplianceAccountUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/compliance/service-agreement")
    @Operation(summary = "Complete compliance service agreement step")
    public ResponseEntity<Void> completeServiceAgreement(@Valid @RequestBody CompleteComplianceServiceAgreementRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        CompleteComplianceServiceAgreementCommand command = new CompleteComplianceServiceAgreementCommand(
            new MerchantId(merchantIdStr),
            request.agreed()
        );
        completeComplianceServiceAgreementUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/compliance/submit")
    @Operation(summary = "Submit compliance for review")
    public ResponseEntity<Void> submitCompliance(Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        SubmitComplianceCommand command = new SubmitComplianceCommand(new MerchantId(merchantIdStr));
        submitComplianceUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
