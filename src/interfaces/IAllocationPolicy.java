package interfaces;

import enums.RequestPriority;
import enums.ResourceType;

public interface IAllocationPolicy {
    int allowed(ResourceType type, RequestPriority priority, IStockView stock);
}
