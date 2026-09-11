package code.filipesz.springdeliveryengine.repositories;

import code.filipesz.springdeliveryengine.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
