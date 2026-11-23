package emergency;

import assets.resources.Reservation;
import assets.resources.ReserveResult;
import enums.EmergencyStatus;
import interfaces.IResourceManager;
import interfaces.IResponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmergencyManager {
    private final IResourceManager resources;
    private final List<Emergency> history = new ArrayList<>();

    public EmergencyManager(IResourceManager resources) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        this.resources = resources;
    }

    public EmergencyStatus handle(Emergency emergency, List<? extends IResponder> candidates) {
        if (emergency == null) throw new IllegalArgumentException("Emergency cannot be null");
        if (candidates == null) throw new IllegalArgumentException("Responder list cannot be null");
        if (emergency.getStatus() != EmergencyStatus.DETECTED)
            throw new IllegalStateException(emergency.getId() + " was already handled (" + emergency.getStatus() + ")");

        emergency.onDetected();

        ResponderRequirement requirement = emergency.getResponderRequirement();
        String shortfall = requirement.shortfall(candidates, emergency.getType());
        if (shortfall != null) return fail(emergency, shortfall);

        ReserveResult result = resources.reserve(emergency.getId(), emergency.getResponseCost(),
                emergency.getType().getPriority());
        if (!result.succeeded()) return fail(emergency, result.failureReason());

        Reservation reservation = result.reservation();
        try {
            resources.commit(reservation);
        } catch (IllegalStateException e) {
            resources.cancel(reservation);
            return fail(emergency, "Reserved resources are no longer available: " + e.getMessage());
        }

        for (IResponder responder : requirement.select(candidates, emergency.getType())) {
            responder.setLocation(emergency.getLocation());
        }

        emergency.onResolved();
        emergency.markResolved();
        history.add(emergency);
        return emergency.getStatus();
    }

    private EmergencyStatus fail(Emergency emergency, String reason) {
        emergency.markUnresolved(reason);
        emergency.onUnresolved();
        history.add(emergency);
        return emergency.getStatus();
    }

    public List<Emergency> getHistory() { return Collections.unmodifiableList(history); }

    public List<Emergency> getByStatus(EmergencyStatus status) {
        return history.stream().filter(e -> e.getStatus() == status).toList();
    }
}