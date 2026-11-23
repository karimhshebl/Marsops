package assets.facilities;

import assets.resources.ResourceInventory;
import static universalHelpers.IdGenerator.generateId;

public class StorageFacility extends Facility{
    public StorageFacility(String name , ResourceInventory resourceInventory) {
        super(generateId("STRG-FAC-"), name, 2, new EntityManager<>(2), resourceInventory);
    }

}
