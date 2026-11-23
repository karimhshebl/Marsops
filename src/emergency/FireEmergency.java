package emergency;

import assets.facilities.Laboratory;
import assets.resources.ResourceInventory;
import assets.resources.ResourceRequirement;
import enums.EmergencyType;
import enums.ResourceType;
import interfaces.ILocation;
import interfaces.IPerson;

public class FireEmergency extends Emergency {
    private static final int EXTRA_OXYGEN_PER_TICK = 2;
    private static final int SMOKE_DAMAGE = 20;

    private final Laboratory laboratory;
    private final ResourceInventory inventory;

    public FireEmergency(Laboratory laboratory, ResourceInventory inventory) {
        super(EmergencyType.FIRE);
        if (laboratory == null) throw new IllegalArgumentException("Laboratory cannot be null");
        if (inventory == null) throw new IllegalArgumentException("Resource inventory cannot be null");
        this.laboratory = laboratory;
        this.inventory = inventory;
    }

    @Override
    public ILocation getLocation() { return laboratory; }

    @Override
    public ResponderRequirement getResponderRequirement() { return new ResponderRequirement(1, 0); }

    @Override
    public ResourceRequirement getResponseCost() {
        return new ResourceRequirement()
                .add(ResourceType.WATER, 10)
                .add(ResourceType.BATTERY_POWER, 5);
    }

    @Override
    public void onDetected() {
        if (!laboratory.isDestroyed() && !laboratory.isUnavailable()) laboratory.makeUnavailable();
        laboratory.damageEquipment();
        inventory.setConsumptionPerTick(ResourceType.OXYGEN,
                inventory.consumptionPerTick(ResourceType.OXYGEN) + EXTRA_OXYGEN_PER_TICK);
        for (IPerson person : laboratory.getPersonnel()) {
            person.changeHealthBy(-SMOKE_DAMAGE);
        }
    }

    @Override
    public void onResolved() {
        if (laboratory.isUnavailable()) laboratory.restore();
        inventory.setConsumptionPerTick(ResourceType.OXYGEN,
                Math.max(0, inventory.consumptionPerTick(ResourceType.OXYGEN) - EXTRA_OXYGEN_PER_TICK));
    }

    @Override
    public void onUnresolved() {
        if (!laboratory.isDestroyed()) laboratory.destroy();
    }

    public Laboratory getLaboratory() { return laboratory; }
}