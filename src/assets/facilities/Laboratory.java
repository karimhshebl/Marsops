package assets.facilities;

import assets.resources.ResourceInventory;
import enums.Equipment;

import java.util.EnumMap;
import java.util.Map;

import static universalHelpers.IdGenerator.generateId;
import static universalHelpers.Validation.validatePositive;

public class Laboratory extends Facility {
    private final Map<Equipment, Integer> equipment = new EnumMap<>(Equipment.class);
    private boolean equipmentDamaged;

    public Laboratory(String name , ResourceInventory resourceInventory) {
        super(generateId("LAB-"), name, 5, new EntityManager<>(5), resourceInventory);
    }

    public void addEquipment(Equipment item, int amount) {
        if (item == null) throw new IllegalArgumentException("Equipment cannot be null");
        validatePositive(amount, "added equipment");
        equipment.merge(item, amount, Integer::sum);
    }

    public int getEquipmentCount(Equipment item) {
        return equipmentDamaged ? 0 : equipment.getOrDefault(item, 0);
    }

    public void damageEquipment() { equipmentDamaged = true; }
    public void repairEquipment() { equipmentDamaged = false; }
    public boolean isEquipmentDamaged() { return equipmentDamaged; }
}
