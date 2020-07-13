package tips.spring.orderservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tips.spring.orderservice.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
