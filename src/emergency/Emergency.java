package emergency;

import assets.resources.ResourceRequirement;
import enums.EmergencyStatus;
import enums.EmergencyType;
import interfaces.ILocation;
import universalHelpers.IdGenerator;
import universalHelpers.Validation;

public abstract class Emergency {
    private final String id = IdGenerator.generateId("EMG-");
    private final EmergencyType type;
    private EmergencyStatus status = EmergencyStatus.DETECTED;
    private String reason;

    protected Emergency(EmergencyType type) {
        if (type == null) throw new IllegalArgumentException("Emergency type cannot be null");
        this.type = type;
    }

    public abstract ILocation getLocation();
    public abstract ResponderRequirement getResponderRequirement();
    public abstract ResourceRequirement getResponseCost();

    public abstract void onDetected();
    public abstract void onResolved();
    public abstract void onUnresolved();

    public void markResolved() {
        requireDetected();
        status = EmergencyStatus.RESOLVED;
    }
    public void markUnresolved(String reason) {
        requireDetected();
        this.reason = Validation.validateString(reason, "Reason");
        status = EmergencyStatus.UNRESOLVED;
    }
    private void requireDetected() {
        if (status != EmergencyStatus.DETECTED)
            throw new IllegalStateException(id + " already ended: " + status);
    }

    public EmergencyType getType() {
        return type;
    }
    public EmergencyStatus getStatus(){
        return status;
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        String text = id + " [" + type + ", " + status + "]";
        return reason == null ? text : text + " reason: " + reason;
    }
}