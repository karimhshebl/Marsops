package gui;

import colony.MarsColony;
import simulation.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class AppState {
    private MarsColony colony;
    private Simulation simulation;
    private final List<Runnable> listeners = new ArrayList<>();
    private Consumer<String> sink = line -> { };
    private final Consumer<String> logger = line -> sink.accept(line);

    void setSink(Consumer<String> sink) { this.sink = sink; }

    Consumer<String> logger() { return logger; }

    void log(String line) { sink.accept(line); }

    MarsColony colony() { return colony; }

    Simulation simulation() { return simulation; }

    void load(MarsColony colony, Simulation simulation) {
        this.colony = colony;
        this.simulation = simulation;
        refresh();
    }

    void newColony(MarsColony colony) {
        load(colony, new Simulation(colony, logger));
    }

    void addListener(Runnable listener) { listeners.add(listener); }

    void refresh() {
        if (colony == null) return;
        for (Runnable listener : listeners) listener.run();
    }
}
