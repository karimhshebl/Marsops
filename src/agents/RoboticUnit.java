package agents;

import enums.AgentKind;
import enums.Capability;
import enums.RobotCondition;
import interfaces.ILocation;
import interfaces.IRobotUnit;
import universalHelpers.IdGenerator;
import universalHelpers.Validation;

import java.util.Set;

public abstract class RoboticUnit extends Agent implements IRobotUnit {
    private static final int BATTERY_DRAINED_PER_WORK_TICK = 10;
    private static final int BATTERY_RECHARGED_PER_IDLE_TICK = 10;

    private int batteryLevel ;
    private RobotCondition robotCondition;

    public RoboticUnit(String name, ILocation location, Set<Capability> capabilities) {
        super(IdGenerator.generateId("ROB-"), name, location, capabilities);

        this.batteryLevel= 100 ;
        this.robotCondition = RobotCondition.OPERATIONAL;
    }

    @Override
    public AgentKind getKind() {
        return AgentKind.ROBOT;
    }

    @Override
    public boolean isUsable() {
        return (batteryLevel>30 )&&!isDamaged();
    }

    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public void changeBatteryLevel(int amount) {
        this.batteryLevel = Validation.clampPercentage(batteryLevel + amount);
    }

    @Override
    public void damage() {
        this.robotCondition = RobotCondition.DAMAGED;
    }

    @Override
    public boolean isDamaged() {
        return robotCondition == RobotCondition.DAMAGED;
    }

    @Override
    public void repair() {
        if (!isDamaged()) throw new IllegalStateException(getName() + " is not damaged. Cannot be repaired");
        this.robotCondition = RobotCondition.OPERATIONAL;
    }

    @Override
    public void tick() {
        if (!isAvailable()) changeBatteryLevel(-BATTERY_DRAINED_PER_WORK_TICK);
        else if (!isDamaged()) changeBatteryLevel(BATTERY_RECHARGED_PER_IDLE_TICK);
    }

    public RobotCondition getRobotCondition() {
        return robotCondition;
    }

    public void setRobotCondition(RobotCondition robotCondition) {
        this.robotCondition = robotCondition;
    }
}
