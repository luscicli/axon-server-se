package io.axoniq.axonserver.message.command.hashing;

import io.axoniq.axonserver.message.command.RoutingSelector;
import io.axoniq.axonserver.message.command.hashing.ConsistentHash.ConsistentHashMember;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Routing selector that select the correct handler based on consistent hash algorithm,
 * where load factor is used as a weight for each handler.
 *
 * @author Sara Pellegrini
 * @since 4.3
 */
public class ConsistentHashRoutingSelector implements RoutingSelector<String> {

    private final AtomicReference<ConsistentHash> consistentHash = new AtomicReference<>(new ConsistentHash());

    private final Function<String, Integer> loadFactorSolver;

    /**
     * Creates an instance that use the specified function in order to resolve the load factor for registered handlers.
     *
     * @param loadFactorSolver a function used to provide the load factor for registered handlers.
     */
    public ConsistentHashRoutingSelector(Function<String, Integer> loadFactorSolver) {
        this.loadFactorSolver = loadFactorSolver;
    }

    @Override
    public Optional<String> selectHandler(String routingKey) {
        return consistentHash.get().getMember(routingKey).map(ConsistentHashMember::getClient);
    }

    @Override
    public void register(String handler) {
        int loadFactor = loadFactorSolver.apply(handler);
        consistentHash.set(consistentHash.get().with(handler, loadFactor));
    }

    @Override
    public void unregister(String handler) {
        consistentHash.set(consistentHash.get().without(handler));
    }
}
