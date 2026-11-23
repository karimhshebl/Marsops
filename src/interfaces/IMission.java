
package interfaces;

import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.EnvironmentCondition;
import enums.MissionState;
import enums.MissionType;
import enums.RequestPriority;

import java.util.List;

public interface IMission extends ITimeDependent,IMissionView {
    String checkSubmission(List<IAgent> candidates, EnvironmentCondition environment);

    void assign(List<IAgent> assignedAgents, EnvironmentCondition environment);

    void start();

    void complete();

    void fail(String reason);

    void applyEnvironment(EnvironmentCondition environment);

}
