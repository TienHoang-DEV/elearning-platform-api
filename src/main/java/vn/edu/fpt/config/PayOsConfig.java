package vn.edu.fpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

/**
 * PayOS Configuration
 * Loads credentials from application.properties
 */
@Configuration
public class PayOsConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    /**
     * Create PayOS client bean from application.properties
     */
    @Bean
    public PayOS payOS() {
        return new PayOS(ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
                .build());
    }

    /**
     * PayOS API base URL for production
     */
    public static final String PAYOS_API_BASE = "https://api-merchant.payos.vn";
}
