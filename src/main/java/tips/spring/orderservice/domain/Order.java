package tips.spring.orderservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "OrderTable")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private Date datetime;

    private String state;

    public OrderStates getOrderState() {
        return OrderStates.valueOf(state);
    }

    public void setOrderState(OrderStates orderState) {
        this.state = orderState.name();
    }
}
