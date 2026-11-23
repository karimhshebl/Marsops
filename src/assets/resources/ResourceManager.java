package assets.resources;

import enums.RequestPriority;
import enums.ResourceType;
import interfaces.IAllocationPolicy;
import interfaces.IResourceManager;
import interfaces.IStockView;

import java.util.Map;
import java.util.Optional;

public class ResourceManager implements IStockView , IResourceManager {
    private final ResourceInventory inventory;
    private final ReservationLedger ledger;
    private final IAllocationPolicy policy;

    public ResourceManager(ResourceInventory inventory, ReservationLedger ledger, IAllocationPolicy policy) {
        this.inventory = inventory;
        this.ledger = ledger;
        this.policy = policy;
    }

    @Override public int total(ResourceType type) { return inventory.total(type); }
    @Override public int free(ResourceType type) { return inventory.total(type) - ledger.reserved(type); }
    @Override public int consumptionPerTick(ResourceType type) { return inventory.consumptionPerTick(type); }

    public int available(ResourceType type, RequestPriority priority) {
        return Math.max(0, policy.allowed(type, priority, this));
    }

    @Override
    public synchronized ReserveResult reserve(String missionId, ResourceRequirement req, RequestPriority priority) {
        for (Map.Entry<ResourceType, Integer> e : req.getAmounts().entrySet()) {
            int allowed = available(e.getKey(), priority);
            if (e.getValue() > allowed) {
                return ReserveResult.failure("Insufficient " + e.getKey() + ": need " + e.getValue() + ", available " + allowed);
            }
        }
        Reservation r = new Reservation(missionId, Map.copyOf(req.getAmounts()));
        ledger.add(r);
        return ReserveResult.success(r);
    }

    public synchronized void commit(Reservation r) {
        ledger.requireOpen(r);
        inventory.withdrawAll(r.amounts());
        ledger.remove(r);
    }

    public synchronized void cancel(Reservation r) {
        ledger.remove(r);
    }
}
