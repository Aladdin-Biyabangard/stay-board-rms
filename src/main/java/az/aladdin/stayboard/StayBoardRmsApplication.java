package az.aladdin.stayboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing(
        dateTimeProviderRef = "hotelAuditingDateTimeProvider",
        auditorAwareRef = "securityAuditorAware"
)
public class StayBoardRmsApplication {

    static void main(String[] args) {
        SpringApplication.run(StayBoardRmsApplication.class, args);
    }

}
