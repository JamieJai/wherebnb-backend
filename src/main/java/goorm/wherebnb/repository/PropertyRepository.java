package goorm.wherebnb.repository;

import goorm.wherebnb.domain.dao.Property;
import goorm.wherebnb.domain.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByPropertyId(Long propertyId);

    Property getPropertyByPropertyId(Long propertyId);

    List<Property> getPropertiesByHost(User host);

    @Query("select p from Property p join fetch p.bookings b where (b.checkOutDate - b.checkInDate) >= 5 and " +
            "(b.checkInDate > :today or (b.checkInDate <= :today and b.checkOutDate >= :fiveDaysFromToday))")
    List<Property> getAvailableProperties(@Param("today") List<Integer> today, @Param("fiveDaysFromToday") LocalDate fiveDaysFromToday);
}