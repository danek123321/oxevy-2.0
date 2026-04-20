package me.alpha432.oxevy.event.system;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Listener wrapper that stores event type for efficient unregistration.
 * Uses record for immutable storage of listener metadata.
 *
 * @author Oxevy Team
 * @version 2.0
 */
public record Listener(Object host, int priority, Consumer<Object> consumer, Class<?> eventType) {
    public static Listener of(Object host, int priority, Method method, Class<?> eventType) {
        return new Listener(host, priority, buildLambdaMetafactory(host, method), eventType);
    }

    // Backward compatible method
    public static Listener of(Object host, int priority, Method method) {
        return new Listener(host, priority, buildLambdaMetafactory(host, method), null);
    }

    public void invoke(Object event) {
        consumer.accept(event);
    }

    public Object getHost() {
        return host;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Object> buildLambdaMetafactory(Object host, Method method) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(host.getClass(), MethodHandles.lookup());
            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(Consumer.class, host.getClass()),
                    MethodType.methodType(void.class, Object.class),
                    lookup.unreflect(method),
                    MethodType.methodType(void.class, method.getParameterTypes()[0])
            );
            MethodHandle target = site.getTarget();
            return (Consumer<Object>) (target.invoke(host));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to build lambda from %s method".formatted(method.getName()), e);
        }
    }
}
