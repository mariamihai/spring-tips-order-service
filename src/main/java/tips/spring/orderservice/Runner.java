package tips.spring.orderservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import tips.spring.orderservice.domain.Order;
import tips.spring.orderservice.domain.OrderEvents;
import tips.spring.orderservice.domain.OrderStates;
import tips.spring.orderservice.services.OrderService;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

    private final StateMachineFactory<OrderStates, OrderEvents> factory;
    private final OrderService orderService;

    private static final String STATE_STRING = "current state: ";

    @Override
    public void run(ApplicationArguments args) {
        Order order = orderService.create(new Date());

        StateMachine<OrderStates, OrderEvents> paymentStateMachine = orderService.pay(order.getId(), UUID.randomUUID().toString());
        log.debug("after calling pay(): " + paymentStateMachine.getState().getId().name());

        StateMachine<OrderStates, OrderEvents> fulfillStateMachine = orderService.fulfill(order.getId());
        log.debug("after calling fulfill(): " + fulfillStateMachine.getState().getId().name());
    }

    /**
     * @deprecated (first part of the tutorial; not needed but good to see for reference)
     */
    @Deprecated (forRemoval = true)
    private void initialCode() {
        long orderId = 1234L;

        StateMachine<OrderStates, OrderEvents> machine = factory.getStateMachine(Long.toString(orderId));
        machine.getExtendedState().getVariables().putIfAbsent("orderId", orderId);

        machine.start();

        log.debug(STATE_STRING + machine.getState().getId().name());

        machine.sendEvent(OrderEvents.PAY);
        log.debug(STATE_STRING + machine.getState().getId().name());

        Message<OrderEvents> message = MessageBuilder
                .withPayload(OrderEvents.FULFILL)
                .setHeader("a", "b")
                .build();
        machine.sendEvent(message);
        log.debug(STATE_STRING + machine.getState().getId().name());

        machine.stop();

    }
}
