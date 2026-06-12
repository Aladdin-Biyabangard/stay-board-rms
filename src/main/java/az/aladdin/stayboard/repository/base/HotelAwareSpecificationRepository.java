package az.aladdin.stayboard.repository.base;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface HotelAwareSpecificationRepository<T, ID>
        extends HotelAwareRepository<T, ID>, JpaSpecificationExecutor<T> {
}
