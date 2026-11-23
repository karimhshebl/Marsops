package assets.equipment;

import interfaces.IRepairable;

import static universalHelpers.IdGenerator.generateId;
import static universalHelpers.Validation.validateString;

public class Equipment implements IRepairable {
    private final String id;
    private final String name;
    private boolean damaged;
    public Equipment(String name){
        this.name = validateString(name , "Equipment Name");
        this.id = generateId("EQP-");
        damaged = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isDamaged() {
        return damaged;
    }

    @Override
    public void damage(){
        if(damaged) throw new IllegalStateException("Equipment already damaged");
        else damaged = true;
    }

    @Override
    public void repair() {
        if(!damaged) throw new IllegalStateException("Equipment doesn't need repair");
        else damaged = false;
    }
}
