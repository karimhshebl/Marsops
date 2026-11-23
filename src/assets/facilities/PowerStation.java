package assets.facilities;

import assets.resources.ResourceInventory;
import static universalHelpers.IdGenerator.generateId;

public class PowerStation extends Facility {
    public PowerStation(String name , ResourceInventory resourceInventory) {
        super(generateId("PWR-FAC-"), name, 5, new EntityManager<>(5), resourceInventory);

    }
}
