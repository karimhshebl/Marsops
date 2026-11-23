package assets.resources;

import enums.ResourceType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static universalHelpers.Validation.validatePositive;

public class    ResourceRequirement {
    private final Map<ResourceType, Integer> amounts = new EnumMap<>(ResourceType.class);

    public ResourceRequirement add(ResourceType type, int quantity) {
        validatePositive(quantity, "Quantity");
        amounts.merge(type, quantity, Integer::sum);
        return this;
    }

    public Map<ResourceType, Integer> getAmounts() {
        return Collections.unmodifiableMap(amounts);
    }
}