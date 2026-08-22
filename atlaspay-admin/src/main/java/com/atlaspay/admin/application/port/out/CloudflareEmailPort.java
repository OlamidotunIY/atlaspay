package com.atlaspay.admin.application.port.out;

public interface CloudflareEmailPort {
    void createEmailRoutingRule(String aliasEmail, String destinationEmail);
}
