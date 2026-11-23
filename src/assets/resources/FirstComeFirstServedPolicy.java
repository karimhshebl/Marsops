package assets.resources;

import enums.RequestPriority;
import enums.ResourceType;
import interfaces.IAllocationPolicy;
import interfaces.IStockView;

public class FirstComeFirstServedPolicy implements IAllocationPolicy {
    @Override
    public int allowed(ResourceType type, RequestPriority priority, IStockView stock) {
        return Math.max(0, stock.free(type));
    }
}
