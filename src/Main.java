import gui.MarsOpsApp;
import scenario.Scenarios;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            for (Scenarios.Scenario scenario : Scenarios.all()) {
                scenario.run(System.out::println);
            }
            return;
        }
        MarsOpsApp.launch();
    }
}
