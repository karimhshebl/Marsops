package interfaces;

public interface IPerson extends IAgent {
    int getHealth();
    int getFatigue();
    void changeHealthBy(int amount);
    void changeFatigueBy(int amount);
}
