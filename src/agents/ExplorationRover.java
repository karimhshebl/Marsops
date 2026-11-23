package agents;

import enums.Capability;
import enums.Equipment;
import interfaces.IEquipmentCarrier;
import interfaces.ILocation;
import universalHelpers.Validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplorationRover extends RoboticUnit implements IEquipmentCarrier {
    private final Map<Equipment, Integer> equipmentInventory = new HashMap<>();

    public ExplorationRover(String name, ILocation location) {
        super(name, location, Set.of(Capability.RESCUE , Capability.COMMUNICATION ,
                Capability.FIRE_FIGHTING, Capability.MEDICAL_CARE, Capability.REPAIR,
                Capability.SURFACE_EXPLORATION , Capability.RESEARCH));
        for (Equipment equipment : Equipment.values()) {
            equipmentInventory.put(equipment, 0);
        }
    }

    public void addEquipment(Equipment equipment, int amount) {
        Validation.validatePositive(amount, "added equipment");
        equipmentInventory.put(equipment, equipmentInventory.getOrDefault(equipment, 0) + amount);
    }

    public boolean consumeEquipment(Equipment equipment, int amount) {
        Validation.validatePositive(amount,"consumed equipment");
        int currentAmount = equipmentInventory.getOrDefault(equipment, 0);
        if (currentAmount >= amount) {
            equipmentInventory.put(equipment, currentAmount - amount);
            return true;
        }
        return false;
    }

    @Override
    public int getEquipmentCount(Equipment equipment) {
        return equipmentInventory.getOrDefault(equipment, 0);
    }

    public Map<Equipment, Integer> getEquipmentInventory() {
        return Collections.unmodifiableMap(equipmentInventory);
    }

}
