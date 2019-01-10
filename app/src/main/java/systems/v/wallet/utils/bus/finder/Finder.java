package systems.v.wallet.utils.bus.finder;

import java.util.Map;
import java.util.Set;

import systems.v.wallet.utils.bus.entity.EventType;
import systems.v.wallet.utils.bus.entity.ProducerEvent;
import systems.v.wallet.utils.bus.entity.SubscriberEvent;

/**
 * Finds producer and subscriber methods.
 */
public interface Finder {

    Map<EventType, ProducerEvent> findAllProducers(Object listener);

    Map<EventType, Set<SubscriberEvent>> findAllSubscribers(Object listener);


    Finder ANNOTATED = new Finder() {
        @Override
        public Map<EventType, ProducerEvent> findAllProducers(Object listener) {
            return AnnotatedFinder.findAllProducers(listener);
        }

        @Override
        public Map<EventType, Set<SubscriberEvent>> findAllSubscribers(Object listener) {
            return AnnotatedFinder.findAllSubscribers(listener);
        }
    };
}
