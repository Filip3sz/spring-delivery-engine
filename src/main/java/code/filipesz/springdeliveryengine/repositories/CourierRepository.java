package code.filipesz.springdeliveryengine.repositories;

import code.filipesz.springdeliveryengine.entities.Courier;
import code.filipesz.springdeliveryengine.entities.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
    List<Courier> findByStatus(CourierStatus status);

    Optional<Courier> findByEmail(String email);
}
