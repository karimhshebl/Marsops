package assets.facilities;

import assets.resources.ResourceInventory;
import static universalHelpers.IdGenerator.generateId;

public class CommunicationStation extends Facility {
    public CommunicationStation(String name , ResourceInventory resourceInventory) {
        super(generateId("COM-FAC-"), name, 5, new EntityManager<>(5), resourceInventory);

    }

}
