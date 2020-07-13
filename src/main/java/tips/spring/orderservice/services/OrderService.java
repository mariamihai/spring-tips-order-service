package tips.spring.orderservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tips.spring.orderservice.domain.OrderEvents;
import tips.spring.orderservice.domain.OrderStates;
import tips.spring.orderservice.domain.Order;
import tips.spring.orderservice.repositories.OrderRepository;

import java.util.Date;
import java.util.Optional;

import static tips.spring.orderservice.config.SimpleEnumStatemachineConfig.ORDER_ID_HEADER;
import static tips.spring.orderservice.config.SimpleEnumStatemachineConfig.PAYMENT_CONFIRMATION_NUMBER_HEADER;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StateMachineFactory<OrderStates, OrderEvents> factory;

    public Order create(Date when) {
        return orderRepository.save(Order.builder().datetime(when).state(OrderStates.SUBMITTED.name()).build());
    }

    @Transactional
    public StateMachine<OrderStates, OrderEvents> pay(Long id, String paymentConfirmationNumber) {
        StateMachine<OrderStates, OrderEvents> stateMachine = build(id);

        Message<OrderEvents> paymentMessage = MessageBuilder.withPayload(OrderEvents.PAY)
                .setHeader(ORDER_ID_HEADER, id)
                .setHeader(PAYMENT_CONFIRMATION_NUMBER_HEADER, paymentConfirmationNumber)
                .build();
        stateMachine.sendEvent(paymentMessage);

        return stateMachine;
    }

    @Transactional
    public StateMachine<OrderStates, OrderEvents> fulfill(Long id) {
        StateMachine<OrderStates, OrderEvents> stateMachine = build(id);

        Message<OrderEvents> fulfillmentMessage = MessageBuilder.withPayload(OrderEvents.FULFILL)
                .setHeader(ORDER_ID_HEADER, id)
                .build();
        stateMachine.sendEvent(fulfillmentMessage);

        return stateMachine;
    }

    private StateMachine<OrderStates, OrderEvents> build(Long id) {
        Order order = orderRepository.getOne(id);
        String orderIdKey = Long.toString(order.getId());
        StateMachine<OrderStates, OrderEvents> stateMachine = factory.getStateMachine(orderIdKey);

        stateMachine.stop();

        stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<>() {
                @Override
                public void preStateChange(State<OrderStates, OrderEvents> state, Message<OrderEvents> message, Transition<OrderStates, OrderEvents> transition, StateMachine<OrderStates, OrderEvents> stateMachine1) {
                    Optional.ofNullable(message).ifPresent(m -> Optional.ofNullable(Long.class.cast(m.getHeaders().get(ORDER_ID_HEADER))).ifPresent(orderId -> {
                        Order order1 = orderRepository.getOne(orderId);
                        order1.setOrderState(state.getId());
                        orderRepository.save(order1);
                    }));
                }
            });
            sma.resetStateMachine(new DefaultStateMachineContext<>(order.getOrderState(), null, null, null));
        });

        stateMachine.start();

        return stateMachine;
    }
}
