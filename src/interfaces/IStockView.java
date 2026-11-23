package interfaces;

import enums.ResourceType;

public interface IStockView {
    int total(ResourceType type);
    int free(ResourceType type);
    int consumptionPerTick(ResourceType type);
}
