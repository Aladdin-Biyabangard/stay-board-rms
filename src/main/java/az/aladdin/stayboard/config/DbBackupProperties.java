package az.aladdin.stayboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "db-backup")
public class DbBackupProperties {

    private String accessKey = "stayboard-db-ops";
    private String mysqlPath = "mysql";
    private long timeoutSeconds = 7200;
    private String zone = "Asia/Baku";
}
