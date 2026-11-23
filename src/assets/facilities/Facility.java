package assets.facilities;

import assets.resources.ItemManager;
import assets.resources.ResourceInventory;
import enums.FacilityStatus;
import enums.ResourceType;
import interfaces.*;

import java.util.List;

import static universalHelpers.Validation.validatePositive;
import static universalHelpers.Validation.validateString;

public abstract class Facility implements IRepairable, ILocation {
    private final String id;
    private final String name;
    private FacilityStatus facilityStatus;
    private final int capacity;
    protected ItemManager<ResourceType> inventory = new ItemManager<>();
    protected IEntityManager<IPerson> personnelManager;

    protected Facility(String id, String name, int capacity,
                       IEntityManager<IPerson> personnelManager,
                       ResourceInventory resourceInventory) {
        if (resourceInventory == null) throw new IllegalArgumentException("Resource inventory must not be null");
        name = validateString(name, "Facility Name");
        capacity = validatePositive(capacity, "Capacity");
        facilityStatus = FacilityStatus.OPERATIONAL;
        this.name = name;
        this.id = id;
        this.capacity = capacity;
        this.personnelManager = personnelManager;
        resourceInventory.register(inventory, this);
    }

    @Override public boolean isSurface() { return false; }
    @Override public String getName() { return name; }
    @Override public boolean isDamaged() { return facilityStatus == FacilityStatus.DAMAGED; }

    @Override
    public void damage() {
        if (isDestroyed()) throw new IllegalStateException(getName() + " is destroyed. Cannot be damaged");
        if (isDamaged()) throw new IllegalStateException(getName() + " is already damaged");
        facilityStatus = FacilityStatus.DAMAGED;
    }

    @Override
    public void repair() {
        if (isDestroyed()) throw new IllegalStateException(getName() + " is destroyed and cannot be repaired");
        if (!isDamaged()) throw new IllegalStateException(getName() + " is not damaged. Cannot be repaired");
        facilityStatus = FacilityStatus.OPERATIONAL;
    }

    public void addPerson(IPerson person) { personnelManager.add(person); }
    public void deletePerson(IPerson person) { personnelManager.delete(person); }
    public long getPersonCount() { return personnelManager.getCount(); }
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public boolean isOperational() { return facilityStatus == FacilityStatus.OPERATIONAL; }
    public int getRobotCapacity() { return capacity; }

    public void destroy() {
        if (facilityStatus == FacilityStatus.DESTROYED) throw new IllegalStateException(getName() + " is already destroyed.");
        else facilityStatus = FacilityStatus.DESTROYED;
    }
    public boolean isDestroyed() { return facilityStatus == FacilityStatus.DESTROYED; }

    public boolean isInventoryAccessible() { return !isDestroyed() && !isUnavailable(); }

    public boolean isUnavailable() { return facilityStatus == FacilityStatus.UNAVAILABLE; }

    public void makeUnavailable() {
        if (isDestroyed()) throw new IllegalStateException(getName() + " is destroyed. Cannot be made unavailable");
        if (isUnavailable()) throw new IllegalStateException(getName() + " is already unavailable");
        facilityStatus = FacilityStatus.UNAVAILABLE;
    }

    public void restore() {
        if (!isUnavailable()) throw new IllegalStateException(getName() + " is not unavailable. Cannot be restored");
        facilityStatus = FacilityStatus.OPERATIONAL;
    }

    public List<IPerson> getPersonnel() { return personnelManager.getAll(); }

    public ItemManager<ResourceType> getInventory() { return inventory; }
}