package assets.resources;

import enums.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ReservationLedger {
    private final Map<ResourceType, Integer> reserved = new EnumMap<>(ResourceType.class);
    private final Set<Reservation> open = Collections.newSetFromMap(new IdentityHashMap<>());

    public synchronized int reserved(ResourceType type) {
        return reserved.getOrDefault(type, 0);
    }

    public synchronized void add(Reservation r) {
        open.add(r);
        r.amounts().forEach((t, q) -> reserved.merge(t, q, Integer::sum));
    }

    public synchronized void requireOpen(Reservation r) {
        if (!open.contains(r)) throw new IllegalStateException("Reservation for " + r.missionId() + " is unknown or already closed");
    }

    public synchronized void remove(Reservation r) {
        requireOpen(r);
        open.remove(r);
        r.amounts().forEach((t, q) -> reserved.merge(t, -q, Integer::sum));
    }
}
