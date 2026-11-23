package emergency;

import assets.facilities.Habitat;
import assets.resources.ResourceRequirement;
import enums.EmergencyType;
import enums.ResourceType;
import interfaces.ILocation;
import universalHelpers.Validation;

public class OxygenLeakEmergency extends Emergency {
    private final Habitat habitat;
    private final int oxygenLoss;
    private int totalOxygenLost;

    public OxygenLeakEmergency(Habitat habitat, int oxygenLoss) {
        super(EmergencyType.OXYGEN_LEAK);
        if (habitat == null) throw new IllegalArgumentException("Habitat cannot be null");
        this.habitat = habitat;
        this.oxygenLoss = Validation.validatePositive(oxygenLoss, "Oxygen loss");
    }

    @Override
    public ILocation getLocation() { return habitat; }

    @Override
    public ResponderRequirement getResponderRequirement() { return new ResponderRequirement(1, 1); }

    @Override
    public ResourceRequirement getResponseCost() {
        return new ResourceRequirement()
                .add(ResourceType.SPARE_PARTS, 2)
                .add(ResourceType.BATTERY_POWER, 5);
    }

    @Override
    public void onDetected() {
        totalOxygenLost += habitat.removeOxygenUpTo(oxygenLoss);
        if (!habitat.isDestroyed() && !habitat.isDamaged()) habitat.damage();
    }

    @Override
    public void onResolved() {
        if (habitat.isDamaged()) habitat.repair();
    }

    @Override
    public void onUnresolved() {
        totalOxygenLost += habitat.removeOxygenUpTo(oxygenLoss);
    }

    public Habitat getHabitat() { return habitat; }
    public int getOxygenLoss() { return oxygenLoss; }
    public int getTotalOxygenLost() { return totalOxygenLost; }
}