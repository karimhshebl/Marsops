package enums;

public enum EmergencyType {
    OXYGEN_LEAK(Capability.REPAIR),
    FIRE(Capability.FIRE_FIGHTING),
    MEDICAL(Capability.MEDICAL_CARE),
    ROVER_BREAKDOWN(Capability.REPAIR);

    private final Capability requiredCapability;

    EmergencyType(Capability requiredCapability) {
        this.requiredCapability = requiredCapability;
    }

    public Capability getRequiredCapability() {
        return requiredCapability;
    }

    public RequestPriority getPriority(){
        switch (this){
            case OXYGEN_LEAK, MEDICAL -> {
                return RequestPriority.LIFE_CRITICAL;
            }
            default -> {
                return RequestPriority.EMERGENCY;
            }
        }
    }
}
