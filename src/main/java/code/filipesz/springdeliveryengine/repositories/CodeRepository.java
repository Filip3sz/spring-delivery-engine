package code.filipesz.springdeliveryengine.repositories;

import code.filipesz.springdeliveryengine.entities.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {
}