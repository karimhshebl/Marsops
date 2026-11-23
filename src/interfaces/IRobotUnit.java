package interfaces;

public interface IRobotUnit extends IAgent, IRepairable {
    int getBatteryLevel();
    void changeBatteryLevel(int amount);
}
