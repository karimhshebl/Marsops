package assets.facilities;

import assets.resources.ResourceInventory;
import enums.ResourceType;

import static universalHelpers.IdGenerator.generateId;

public class Habitat extends Facility{
    public Habitat(String name , ResourceInventory resourceInventory) {
        super(generateId("HAB-"), name, 20, new EntityManager<>(20), resourceInventory);
    }

    public void removeOxygen(int amount){
        if (!isInventoryAccessible()) throw new IllegalStateException(getName() + " is destroyed. Its oxygen is unavailable");
        inventory.removeResource(ResourceType.OXYGEN, amount);
    }

    public int removeOxygenUpTo(int amount){
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative");
        if (!isInventoryAccessible()) return 0;
        int removed = Math.min(amount, inventory.getQuantity(ResourceType.OXYGEN));
        if (removed > 0) inventory.removeResource(ResourceType.OXYGEN, removed);
        return removed;
    }
}
