package com.atlaspay.identity.application.command;


public record CompleteComplianceContactCommand(
    Long merchantId,
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
