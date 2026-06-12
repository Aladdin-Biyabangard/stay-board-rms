package az.aladdin.stayboard.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface HotelAwareRepository<T, ID> extends JpaRepository<T, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.hotelId = :hotelId")
    List<T> findAllByHotelId(Long hotelId);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.hotelId = :hotelId")
    Optional<T> findByIdAndHotelId(ID id, Long hotelId);
}
