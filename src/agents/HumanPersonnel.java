package agents;

import enums.AgentKind;
import enums.Capability;
import interfaces.ILocation;
import interfaces.IPerson;
import universalHelpers.IdGenerator;
import universalHelpers.Validation;

import java.util.Set;

public abstract class HumanPersonnel extends Agent implements IPerson {
    private static final int FATIGUE_GAINED_PER_WORK_TICK = 10;
    private static final int FATIGUE_RECOVERED_PER_REST_TICK = 10;

    private int health ;
    private int fatigue;

    public HumanPersonnel(String name, ILocation location, Set<Capability> capabilities) {
        super(IdGenerator.generateId("HUM-"), name, location, capabilities);

        health = 100 ;
        fatigue = 0;
    }

    @Override
    public AgentKind getKind(){
        return AgentKind.HUMAN ;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getFatigue() {
        return fatigue;
    }

    @Override
    public boolean isUsable(){
        return health >= 50 && fatigue <= 50;
    }

    @Override
    public void changeHealthBy(int amount) {
        health = Validation.clampPercentage(health + amount);
    }

    @Override
    public void changeFatigueBy(int amount){
        fatigue = Validation.clampPercentage(fatigue + amount);
    }

    @Override
    public void tick() {
        changeFatigueBy(isAvailable() ? -FATIGUE_RECOVERED_PER_REST_TICK : FATIGUE_GAINED_PER_WORK_TICK);
    }
}
