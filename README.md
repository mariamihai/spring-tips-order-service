# Simple project for understanding Spring Statemachine
The project follows [this](https://www.youtube.com/watch?v=M4Aa45Gpc4w) Spring Tips tutorial.

Contains a simple Order object and transitions it between several states based on different triggered events using Spring Statemachine.

## Description
**Statemachine** = anything where you have orderly or predictable deterministic transitions from one state to another and then have actions associated with those.

Statemachine extricates the flow, the progression of the business object from one state to another, into a separate place where you can model it and then have the business logic associated with that respond to those state changes.

It helps keep the business logic in a single place and have some actions describing what should happen based on that state.

This is useful when you need to change the workflow, you can do so in a single place as opposed to having to scour all the different layers in the codebase where you have all these complex decisions.

## Code description
- **@EnableStateMachineFactory** - refers to the ability of the Spring Statemachine to either keep one sort of global statemachine or to vend new instances of the statemachine. The factory enables you to get new instances.

- **StateMachineConfigurationConfigurer** is the engine itself

- override **configure(StateMachineStateConfigurer)** to tell the statemachine about the states

- override **configure(StateMachineTransitionConfigurer)** to configure the transitions
    - if you want to go from one state to another, in order (from a to z) - _withLocal()_
    - change the state in response to some sort of event - _withExternal()_

## Links
- [Spring Tips: Spring Statemachine](https://www.youtube.com/watch?v=M4Aa45Gpc4w)
- [spring.io](https://projects.spring.io/spring-statemachine/)

## Other projects
- [Activiti](https://spring.io/blog/2015/03/08/getting-started-with-activiti-and-spring-boot)
- [Flowable](https://www.youtube.com/watch?v=43_OLrxU3so)
