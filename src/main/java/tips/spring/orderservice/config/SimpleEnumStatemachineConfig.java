package tips.spring.orderservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import tips.spring.orderservice.domain.OrderEvents;
import tips.spring.orderservice.domain.OrderStates;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class SimpleEnumStatemachineConfig extends StateMachineConfigurerAdapter<OrderStates, OrderEvents> {

    public static final String ORDER_ID_HEADER = "orderId";
    public static final String PAYMENT_CONFIRMATION_NUMBER_HEADER = "paymentConfirmationNumber";

    // describe the transitions from one state to another
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions) throws Exception {
        transitions
                .withExternal().source(OrderStates.SUBMITTED).target(OrderStates.PAID).event(OrderEvents.PAY)
                .and()
                .withExternal().source(OrderStates.PAID).target(OrderStates.FULFILLED).event(OrderEvents.FULFILL)
                .and()
                .withExternal().source(OrderStates.SUBMITTED).target(OrderStates.CANCELLED).event(OrderEvents.CANCEL)
                .and()
                .withExternal().source(OrderStates.PAID).target(OrderStates.CANCELLED).event(OrderEvents.CANCEL);
    }

    // tell the statemachine about the different states
    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states) throws Exception {
        states.withStates()
                .initial(OrderStates.SUBMITTED)
                .stateEntry(OrderStates.SUBMITTED, new Action<OrderStates, OrderEvents>() {
                    @Override
                    public void execute(StateContext<OrderStates, OrderEvents> context) {
                        Long orderId = Long.class.cast(context.getExtendedState().getVariables().getOrDefault("orderId", -1L));
                        log.debug("orderId is: " + orderId);
                        log.debug("entering SUBMITTED state");
                    }
                })
                .state(OrderStates.PAID)
                .end(OrderStates.FULFILLED)
                .end(OrderStates.CANCELLED);
    }

    // configure the engine itself
    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderEvents> config) throws Exception {
        StateMachineListenerAdapter<OrderStates, OrderEvents> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<OrderStates, OrderEvents> from, State<OrderStates, OrderEvents> to) {
                log.debug(String.format("stateChange from: %s, to: %s", from.getId().name(), to.getId().name()));
            }
        };

        config.withConfiguration() // change the configuration
            .autoStartup(false) // startup automatically
            .listener(adapter); // provide a listener
    }
}
