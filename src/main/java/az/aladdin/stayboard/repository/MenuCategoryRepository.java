package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.MenuCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategoryEntity, Long>, JpaSpecificationExecutor<MenuCategoryEntity> {

    Optional<MenuCategoryEntity> findByIdAndHotelId(Long id, Long hotelId);

    boolean existsByIdAndHotelId(Long id, Long hotelId);
}
