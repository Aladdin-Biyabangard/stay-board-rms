package az.aladdin.stayboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stay-board")
public class PmsServiceAuthProperties {

    /**
     * Shared secret for RMS → PMS server-to-server calls (see {@code application.yaml}).
     */
    private String internalApiKey;

    /**
     * Optional static staff JWT fallback when internal API key is not configured.
     */
    private String serviceToken;

    public boolean hasInternalApiKey() {
        return internalApiKey != null && !internalApiKey.isBlank();
    }

    public boolean hasServiceToken() {
        return serviceToken != null && !serviceToken.isBlank();
    }
}
