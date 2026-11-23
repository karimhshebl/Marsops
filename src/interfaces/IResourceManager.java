package interfaces;

import assets.resources.Reservation;
import assets.resources.ReserveResult;
import assets.resources.ResourceRequirement;
import enums.RequestPriority;
import enums.ResourceType;

public interface IResourceManager {
    int available(ResourceType type, RequestPriority priority);
    ReserveResult reserve(String missionId, ResourceRequirement requirement, RequestPriority priority);
    void commit(Reservation reservation);
    void cancel(Reservation reservation);
}