package core.shared;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot<ID> {

    private final ID id;                             // identity; set exactly once, at construction, never reassigned
    private final List<DomainEvent> pendingEvents; // uncommitted events raised by this aggregate
    private long version;                            // optimistic-lock token; incremented by the persistence adapter on each successful save

    protected AggregateRoot(ID id) {
        this.id = id;
        this.pendingEvents = new ArrayList<>();
    }

    public ID id() {
        return id;
    }

    protected void register(DomainEvent event) {
        pendingEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public long version() {
        return version;
    }

    protected void version(long version) {
        this.version = version;
    }
}
