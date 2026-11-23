package assets.resources;

import enums.RequestPriority;
import enums.ResourceType;
import enums.SupplyLevel;
import interfaces.IAllocationPolicy;
import interfaces.IStockView;

import java.util.Set;

public class TieredAllocationPolicy implements IAllocationPolicy {
    private static final Set<ResourceType> LIFE_SUPPORT =
            Set.of(ResourceType.OXYGEN, ResourceType.WATER, ResourceType.FOOD);
    private static final Set<ResourceType> EMERGENCY_STOCK =
            Set.of(ResourceType.MEDICAL_SUPPLY, ResourceType.SPARE_PARTS);

    private static final int FLOOR_TICKS = 48;
    private static final double EMERGENCY_SHARE = 0.30;
    private static final int NORMAL_ABOVE = 72;
    private static final int CRITICAL_BELOW = 24;

    @Override
    public int allowed(ResourceType type, RequestPriority priority, IStockView stock) {
        int free = stock.free(type);
        if (LIFE_SUPPORT.contains(type) && priority != RequestPriority.LIFE_CRITICAL) {
            free -= stock.consumptionPerTick(type) * FLOOR_TICKS;
        } else if (EMERGENCY_STOCK.contains(type) && priority == RequestPriority.ROUTINE) {
            free -= (int) (stock.total(type) * EMERGENCY_SHARE);
        }
        free = Math.max(0, free);

        if (priority == RequestPriority.ROUTINE) {
            switch (supplyLevel(type, stock)) {
                case CRITICAL: return 0;
                case CONSERVE: return free / 2;
                default: break;
            }
        }
        return free;
    }

    public SupplyLevel supplyLevel(ResourceType type, IStockView stock) {
        int rate = stock.consumptionPerTick(type);
        if (rate == 0) return SupplyLevel.NORMAL;
        int ticks = stock.total(type) / rate;
        if (ticks > NORMAL_ABOVE) return SupplyLevel.NORMAL;
        return ticks < CRITICAL_BELOW ? SupplyLevel.CRITICAL : SupplyLevel.CONSERVE;
    }
}
