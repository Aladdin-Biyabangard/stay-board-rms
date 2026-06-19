package az.aladdin.stayboard.config.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stay-board.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int generalRequestsPerMinute = 300;
    private int authRequestsPerMinute = 30;
    private int burstCapacity = 50;
}
