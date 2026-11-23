package interfaces;

import enums.EnvironmentCondition;

public interface IEnvironmentSensitive {
    void applyEnvironment(EnvironmentCondition environment);
}
