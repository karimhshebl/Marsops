package interfaces;

import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.MissionState;
import enums.MissionType;
import enums.RequestPriority;

import java.util.List;

public interface IMissionView {
    String getId();

    MissionType getType();

    MissionState getState();

    RequestPriority getPriority();

    int getDuration();

    int getElapsed();

    int getRemaining();

    ResourceRequirement getRequirement();

    List<Facility> getTargets();

    List<IAgent> getAgents();

    String getFailureReason();
    boolean isActive();

    boolean isFinished();

}
