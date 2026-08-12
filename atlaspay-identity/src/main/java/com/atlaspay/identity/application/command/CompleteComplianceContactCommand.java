package com.atlaspay.identity.application.command;

import com.atlaspay.shared.domain.id.MerchantId;

public record CompleteComplianceContactCommand(
    MerchantId merchantId,
    String supportEmail,
    String disputeEmail,
    String whatsappPhone,
    String whatsappName,
    String websiteUrl,
    String twitterHandle,
    String facebookUsername,
    String instagramHandle,
    String businessState,
    String businessLga,
    String businessCity,
    String businessStreet
) {}
