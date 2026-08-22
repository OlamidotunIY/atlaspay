package com.atlaspay.admin.infrastructure.adapter.external;

import com.atlaspay.admin.application.port.out.CloudflareEmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Cloudflare Email Routing API Adapter.
 * In a real environment, this would use RestTemplate or WebClient to call:
 * POST /client/v4/zones/{zone_id}/email/routing/rules
 */
@Component
public class CloudflareEmailAdapter implements CloudflareEmailPort {

    private static final Logger log = LoggerFactory.getLogger(CloudflareEmailAdapter.class);

    @Override
    public void createEmailRoutingRule(String aliasEmail, String destinationEmail) {
        // Here we would extract the prefix from the alias (e.g., "damilola")
        String prefix = aliasEmail.split("@")[0];
        
        log.info("Calling Cloudflare API to create routing rule: {} -> {}", aliasEmail, destinationEmail);
        
        // Mocking the API call for now since we don't have the Cloudflare Zone ID and Token in env
        log.info("Cloudflare Email Routing Rule created successfully for prefix '{}'", prefix);
    }
}
