package assets.resources;

import assets.facilities.Facility;
import enums.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceInventory {
    private final Map<ItemManager<ResourceType>, Facility> inventories = new LinkedHashMap<>();
    private final Map<ResourceType, Integer> consumptionPerTick = new EnumMap<>(ResourceType.class);

    public synchronized void register(ItemManager<ResourceType> manager, Facility facility) {
        if (manager == null || facility == null) throw new IllegalArgumentException("Manager and Facility must not be null");
        inventories.put(manager, facility);
    }

    public synchronized void unregister(ItemManager<ResourceType> manager) {
        inventories.remove(manager);
    }

    public synchronized int total(ResourceType type) {
        if (type == null) throw new IllegalArgumentException("Resource type cannot be null");
        int sum = 0;
        for (Map.Entry<ItemManager<ResourceType>, Facility> e : inventories.entrySet()) {
            if (e.getValue().isInventoryAccessible()) sum += e.getKey().getQuantity(type);
        }
        return sum;
    }

    public synchronized Map<ResourceType, Integer> totals() {
        Map<ResourceType, Integer> result = new EnumMap<>(ResourceType.class);
        for (ResourceType t : ResourceType.values()) result.put(t, total(t));
        return Collections.unmodifiableMap(result);
    }

    public synchronized void setConsumptionPerTick(ResourceType type, int amount) {
        consumptionPerTick.put(type, amount);
    }

    public synchronized int consumptionPerTick(ResourceType type) {
        return consumptionPerTick.getOrDefault(type, 0);
    }

    public synchronized void withdrawAll(Map<ResourceType, Integer> amounts) {
        amounts.forEach((type, qty) -> {
            if (total(type) < qty) throw new IllegalStateException("Not enough " + type + " in stock");
        });
        amounts.forEach(this::withdraw);
    }

    private void withdraw(ResourceType type, int qty) {
        for (Map.Entry<ItemManager<ResourceType>, Facility> e : inventories.entrySet()) {
            if (!e.getValue().isInventoryAccessible()) continue;
            ItemManager<ResourceType> m = e.getKey();
            int take = Math.min(qty, m.getQuantity(type));
            if (take > 0) { m.removeResource(type, take); qty -= take; }
            if (qty == 0) return;
        }
    }
}
